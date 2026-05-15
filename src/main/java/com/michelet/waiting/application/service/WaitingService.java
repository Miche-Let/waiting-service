package com.michelet.waiting.application.service;

import com.michelet.waiting.application.dto.EnterWaitingCommand;
import com.michelet.waiting.application.dto.GetWaitingStatusQuery;
import com.michelet.waiting.application.dto.WaitingResult;
import com.michelet.waiting.application.port.ScoredToken;
import com.michelet.waiting.application.port.WaitingActivationPort;
import com.michelet.waiting.domain.entity.Waiting;
import com.michelet.waiting.domain.entity.WaitingOutbox;
import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;
import com.michelet.waiting.domain.repository.WaitingOutboxRepository;
import com.michelet.waiting.domain.repository.WaitingRepository;
import com.michelet.waiting.infrastructure.persistence.jpa.WaitingOutboxSaver;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final WaitingOutboxRepository waitingOutboxRepository;
    private final WaitingActivationPort waitingActivationPort;
    private final WaitingOutboxSaver waitingOutboxSaver;

    @Value("${waiting.activate-ratio:0.1}")
    private double activateRatio;

    @Value("${waiting.expire-minutes:10}")
    private int expireMinutes;

    @Value("${waiting.outbox-max-retry:5}")
    private int outboxMaxRetry;

    private static final UUID SYSTEM_UUID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    // 대기 등록
    public WaitingResult enterWaiting(EnterWaitingCommand command){

        waitingRepository.findWaitingByRestaurantId(command.restaurantId())
                .stream()
                .filter(w -> w.getUserId().equals(command.userId()))
                .findAny()
                .ifPresent(w ->{throw new WaitingException(WaitingErrorCode.ALREADY_IN); });

        Waiting waiting = Waiting.create(command.userId(), command.restaurantId());

        // DB 저장
        Waiting saved = waitingRepository.save(waiting);

        // redis 순번 등록
        waitingActivationPort.add(command.restaurantId(), saved.getToken().value());
        // redis 순번 조회
        Long position = waitingActivationPort.getPosition(
                command.restaurantId(), waiting.getToken().value()
        );


        return WaitingResult.of(saved, position);

    }

    // 상태 조회
    @Transactional(readOnly = true)
    public WaitingResult getStatus(GetWaitingStatusQuery query){
        Waiting waiting = waitingRepository.findByToken(query.token())
                .orElseThrow(() -> new WaitingException(WaitingErrorCode.NOT_FOUND));

        if(waiting.getStatus() == WaitingStatus.WAITING){
            Long position = waitingActivationPort.getPosition(
                    waiting.getRestaurantId(), query.token()
            );
            return WaitingResult.of(waiting, position);
        }
        return WaitingResult.of(waiting);
    }

    // 취소
    public void cancelWaiting(UUID waitingId, UUID deletedBy){
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new WaitingException(WaitingErrorCode.NOT_FOUND));

        if(!waiting.getUserId().equals(deletedBy)){
            throw new WaitingException(WaitingErrorCode. FORBIDDEN);
        }

        waiting.cancel();
        waitingRepository.save(waiting);
        waitingRepository.softDelete(waitingId, deletedBy);
        waitingActivationPort.remove(waiting.getRestaurantId(), waiting.getToken().value());
    }

    // ACTIVE 상태인지 검증 - 예약 서비스가 예약 전 호출
    @Transactional(readOnly = true)
    public WaitingResult verifyToken(String accessToken){
        if(accessToken == null || accessToken.isBlank())
            throw new WaitingException(WaitingErrorCode.INVALID_TOKEN);

        Waiting waiting = waitingRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new WaitingException(WaitingErrorCode.NOT_FOUND));
        // 상태 검증
        if(!waiting.isActive())
            throw new WaitingException(WaitingErrorCode.INVALID_STATE);
        // 유효성 검증
        if(!waiting.isValidAccessToken(accessToken))
            throw new WaitingException(WaitingErrorCode.INVALID_TOKEN);

        return WaitingResult.of(waiting);
    }

    // 토큰 삭제 - 예약 서비스가 예약 완료 후 호출
    public void completeWaiting(UUID waitingId){
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new WaitingException(WaitingErrorCode.NOT_FOUND));
        if(!waiting.isActive())
            throw new WaitingException(WaitingErrorCode.INVALID_STATE);

        waitingRepository.softDelete(waitingId, SYSTEM_UUID);

        waitingActivationPort.remove(waiting.getRestaurantId(), waiting.getToken().value());
    }

    // 스케줄러 - N명씩 입장 허용
    public void activateNextBatch(UUID restaurantId){

        Long totalWaiting = waitingActivationPort.countWaiting(restaurantId);

        if(totalWaiting == 0) return;

        int batchSize = (int) Math.max(1, Math.round(totalWaiting * activateRatio));

        List<ScoredToken> scoredTokens = waitingActivationPort.popNextTokensWithScore(restaurantId,batchSize);

        for(ScoredToken scoredToken : scoredTokens){
            try{
                Optional<Waiting> waitingOpt = waitingRepository.findByToken(scoredToken.token());
                if (waitingOpt.isEmpty()) {
                    // 토큰이 DB에 없으면 Redis 복구 후 다음 토큰으로
                    waitingActivationPort.addWithScore(
                            restaurantId,
                            scoredToken.token(),
                            scoredToken.score()
                    );
                    log.warn("[스케줄러] 토큰 {} 에 해당하는 대기 엔티티 없음 - Redis 복구",
                            scoredToken.token());
                    continue;
                }
                Waiting waiting = waitingOpt.get();
                // 1. Outbox PENDING 생성 + 저장 (waitingId 포함)
                WaitingOutbox outbox = WaitingOutbox.create(
                        waiting.getId(),
                        scoredToken.token(),
                        restaurantId,
                        scoredToken.score()
                );
                waitingOutboxSaver.save(outbox);

                // 2. DB ACTIVE 전환
                waiting.activate();
                waitingRepository.save(waiting);

                // 3. 동일한 Outbox 인스턴스 PROCESSED 로 update
                outbox.markProcessed(LocalDateTime.now());
                waitingOutboxRepository.update(outbox);
            }catch (Exception e){
                // 4. DB 저장 실패 시 원래 score로 Redis 복구
                waitingActivationPort.addWithScore(
                        restaurantId,
                        scoredToken.token(),
                        scoredToken.score()
                );

                log.warn("[스케줄러] ACTIVE 전환 실패 Redis 복구 - token : {}",
                        scoredToken.token(),e);
            }
        }
    }

    // 스케줄러 - 만료 처리
    public void expireWaitings(){
        List<Waiting> expired = waitingRepository
                .findExpiredActives(LocalDateTime.now().minusMinutes(expireMinutes));
        expired.forEach(waiting -> {
            waiting.expire();
            waitingRepository.save(waiting);
            waitingActivationPort.remove(waiting.getRestaurantId(), waiting.getToken().value());
        });
    }

    public void retryPendingOutbox(){

        List<WaitingOutbox> pendingList =
                waitingOutboxRepository.findPendingOrFailed();

        for (WaitingOutbox outbox : pendingList) {
            // 재시도 한계 초과 시 ABANDONED 처리
            if (outbox.isExceededRetryLimit(outboxMaxRetry)) {
                log.error("[스케줄러] Outbox 재시도 한계 초과 ABANDONED - outboxId: {}",
                        outbox.getOutboxId());
                outbox.markAbandoned(LocalDateTime.now());
                waitingOutboxRepository.update(outbox);
                continue;
            }
            try {
                Optional<Waiting> waitingOpt =
                        waitingRepository.findByToken(outbox.getToken());

                if (waitingOpt.isEmpty()) {
                    log.warn("[스케줄러] Outbox 재처리 - 토큰 {} 에 해당하는 대기 엔티티 없음",
                            outbox.getToken());
                    outbox.markProcessed(LocalDateTime.now());
                    waitingOutboxRepository.update(outbox);
                    continue;
                }

                Waiting waiting = waitingOpt.get();

                if (waiting.getStatus() == WaitingStatus.WAITING) {
                    waitingActivationPort.remove(
                            outbox.getRestaurantId(),
                            outbox.getToken()
                    );
                    waiting.activate();
                    waitingRepository.save(waiting);
                }

                outbox.markProcessed(LocalDateTime.now());
                waitingOutboxRepository.update(outbox);

            } catch (Exception e) {
                log.error("[스케줄러] Outbox 재처리 실패 - outboxId: {}",
                        outbox.getOutboxId(), e);
                outbox.markFailed(LocalDateTime.now());
                waitingOutboxRepository.update(outbox);
            }
        }
    }

}
