package com.michelet.waiting.infrastructure.scheduler;

import com.michelet.waiting.application.service.WaitingService;
import com.michelet.waiting.domain.entity.WaitingOutbox;
import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.domain.repository.WaitingOutboxRepository;
import com.michelet.waiting.domain.repository.WaitingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingScheduler {

    private final WaitingService waitingService;
    private final WaitingRepository waitingRepository;
    private final WaitingOutboxRepository waitingOutboxRepository;

    // N명씩 입장 허용
    // 모든 식당의 대기열을 순회하며 10% 인원 활성화
    @Scheduled(fixedDelayString = "${waiting.activate-scheduler-delay-ms:10000}")
    public void activateNextBatch() {
        log.info("[스케줄러] activateNextBatch 실행");
        List<UUID> restaurantIds = waitingRepository.findDistinctRestaurantIdsWithWaiting();
        for (UUID restaurantId : restaurantIds) {
            try {
                waitingService.activateNextBatch(restaurantId);
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    log.warn("[스케줄러] activateNextBatch 인터럽트 - restaurantId: {}", restaurantId, e);
                    break;
                }
                log.warn("[스케줄러] activateNextBatch 실패 - restaurantId: {}", restaurantId, e);
            }
        }
    }
    // 만료 처리
    @Scheduled(fixedDelayString = "${waiting.expire-scheduler-delay-ms:60000}")
    public void expireWaitings(){
        log.info("[스케줄러] expireWaitings 실행");
        try {
            waitingService.expireWaitings();
        }catch (Exception e){
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("[스케줄러] expireWaitings 실패", e);
        }
    }

    // PENDING 상태 Outbox 재처리
    @Scheduled(fixedDelayString = "${waiting.outbox-retry-delay-ms:30000}")
    public void retryPendingOutbox(){
        log.info("[스케줄러] Outbox PENDING 재처리 실행");

        List<WaitingOutbox> pendingList = waitingOutboxRepository.findPendingOrFailed();

        for(WaitingOutbox outbox : pendingList) {
            try{
                waitingRepository.findByToken(outbox.getToken())
                        .ifPresent(waiting -> {
                            if(waiting.getStatus() == WaitingStatus.WAITING){
                                waiting.activate();
                                waitingRepository.save(waiting);
                            }
                            outbox.markProcessed(LocalDateTime.now());
                            waitingOutboxRepository.update(outbox);
                        });
            }catch (Exception e){
                log.error("[스케줄러] Outbox 재처리 실패 - outboxId: {}",
                        outbox.getOutboxId(), e);
            }
        }
    }


}
