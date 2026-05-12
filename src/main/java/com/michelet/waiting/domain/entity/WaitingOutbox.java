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
    private LocalDateTime processedAt;

    public WaitingOutbox(UUID outboxId, UUID waitingId, String token, UUID restaurantId, Long score, OutboxStatus status) {
        this.outboxId = outboxId;
        this.waitingId = waitingId;
        this.token = token;
        this.restaurantId = restaurantId;
        this.score = score;
        this.status = status;
    }
    public static WaitingOutbox create(UUID waitingId, String token, UUID restaurantId, Long score){
        return new WaitingOutbox(
                UUID.randomUUID(), waitingId, token, restaurantId, score, OutboxStatus.PENDING
        );
    }

    public void markProcessed () {
        this.status = OutboxStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }
    public void markFailed() {
        this.status = OutboxStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }
    public boolean isPending() {
        return OutboxStatus.PENDING == status;
    }

}
