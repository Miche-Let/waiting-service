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
import java.time.LocalDateTime;
import java.util.List;
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

    @Value("${waiting.activate-ratio:0.1}")
    private double activateRatio;

    @Value("${waiting.expire-minutes:10}")
    private int expireMinutes;

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

            // 1. Outbox 에 PENDING 이벤트 기록
            WaitingOutbox outbox = WaitingOutbox.create(
                    null,
                    scoredToken.token(),
                    restaurantId,
                    scoredToken.score()
            );

            try{
                waitingRepository.findByToken(scoredToken.token())
                        .ifPresent(waiting -> {

                            // 2. DB ACTIVE 전환
                            waiting.activate();
                            waitingRepository.save(waiting);

                            // 3. Outbox PROCESSED 기록
                            WaitingOutbox processedOutbox = WaitingOutbox.create(
                                    waiting.getId(),
                                    scoredToken.token(),
                                    restaurantId,
                                    scoredToken.score()
                            );
                            processedOutbox.markProcessed();
                            waitingOutboxRepository.save(processedOutbox);

                        });
            }catch (Exception e){
                if(e instanceof InterruptedException){
                    Thread.currentThread().interrupt();
                    break;
                }

                // 4. DB 저장 실패 시 원래 score로 Redis 복구
                waitingActivationPort.addWithScore(
                        restaurantId,
                        scoredToken.token(),
                        scoredToken.score()
                );

                // 5. Outbox FAILED 기록
                outbox.markFailed();
                waitingOutboxRepository.save(outbox);

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
}
