package com.michelet.waiting.application.service;

import com.michelet.waiting.application.dto.EnterWaitingCommand;
import com.michelet.waiting.application.dto.GetWaitingStatusQuery;
import com.michelet.waiting.application.dto.WaitingResult;
import com.michelet.waiting.application.port.WaitingActivationPort;
import com.michelet.waiting.domain.entity.Waiting;
import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;
import com.michelet.waiting.domain.repository.WaitingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final WaitingActivationPort waitingActivationPort;

    @Value("${waiting.activate-ratio:0.1}")
    private double activateRatio;

    @Value("${waiting.expire-minutes:10}")
    private int expireMinutes;

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

        waiting.cancel();
        waitingRepository.save(waiting);
        waitingRepository.softDelete(waitingId, deletedBy);
        waitingActivationPort.remove(waiting.getRestaurantId(), waiting.getToken().value());
    }

    // 스케줄러 - N명씩 입장 허용
    public void activateNextBatch(UUID restaurantId){

        Long totalWaiting = waitingActivationPort.countWaiting(restaurantId);

        if(totalWaiting == 0) return;

        int batchSize = (int) Math.max(1, Math.round(totalWaiting * activateRatio));

        List<String> tokens = waitingActivationPort.popNextTokens(restaurantId, batchSize);


        tokens.forEach(token ->
                waitingRepository.findByToken(token).ifPresent(waiting -> {
                    waiting.activate();
                    waitingRepository.save(waiting);
                }));
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
