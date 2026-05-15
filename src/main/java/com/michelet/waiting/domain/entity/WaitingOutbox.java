package com.michelet.waiting.domain.entity;

import com.michelet.waiting.domain.enums.OutboxStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class WaitingOutbox {
    private final UUID outboxId;
    private final UUID waitingId;
    private final String token;
    private final UUID restaurantId;
    private final Long score;
    private OutboxStatus status;
    private int retryCount;
    private LocalDateTime processedAt;

    public WaitingOutbox(UUID outboxId, UUID waitingId, String token, UUID restaurantId, Long score, OutboxStatus status,int retryCount) {
        this.outboxId = outboxId;
        this.waitingId = waitingId;
        this.token = token;
        this.restaurantId = restaurantId;
        this.score = score;
        this.status = status;
        this.retryCount = retryCount;
    }
    public static WaitingOutbox create(UUID waitingId, String token, UUID restaurantId, Long score){
        return new WaitingOutbox(
                UUID.randomUUID(), waitingId, token, restaurantId, score, OutboxStatus.PENDING,0
        );
    }

    public static WaitingOutbox restore(UUID outboxId, UUID waitingId, String token,
                                        UUID restaurantId, Long score,
                                        OutboxStatus status, int retryCount,
                                        LocalDateTime processedAt){
        WaitingOutbox outbox = new WaitingOutbox(
                outboxId, waitingId, token, restaurantId, score,status, retryCount
        );
        outbox.processedAt = processedAt;
        return outbox;
    }

    public void markProcessed (LocalDateTime processedAt) {
        this.status = OutboxStatus.PROCESSED;
        this.processedAt = processedAt;
    }

    public void markFailed (LocalDateTime processedAt) {
        this.status = OutboxStatus.FAILED;
        this.retryCount = this.retryCount + 1 ;
        this.processedAt = processedAt;
    }

    public void markAbandoned(LocalDateTime processedAt) {
        this.status      = OutboxStatus.ABANDONED;
        this.processedAt = processedAt;
    }

    // 재시도 한계 초과 여부
    public boolean isExceededRetryLimit(int maxRetry) {
        return this.retryCount >= maxRetry;
    }

}
