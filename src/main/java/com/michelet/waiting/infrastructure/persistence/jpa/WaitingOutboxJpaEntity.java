package com.michelet.waiting.infrastructure.persistence.jpa;

import com.michelet.common.entity.BaseEntity;
import com.michelet.waiting.domain.entity.WaitingOutbox;
import com.michelet.waiting.domain.enums.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_waiting_outbox")
public class WaitingOutboxJpaEntity extends BaseEntity {

    @Id
    @Column(name = "outbox_id", columnDefinition = "uuid")
    private UUID outboxId;

    @Column(name = "waiting_id", columnDefinition = "uuid")
    private UUID waitingId;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "restaurant_id", columnDefinition = "uuid")
    private UUID restaurantId;

    @Column(name = "score", nullable = false)
    private Long score;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public static WaitingOutboxJpaEntity from(WaitingOutbox outbox){
        WaitingOutboxJpaEntity e = new WaitingOutboxJpaEntity();
        e.outboxId = outbox.getOutboxId();
        e.waitingId = outbox.getWaitingId();
        e.token = outbox.getToken();
        e.restaurantId = outbox.getRestaurantId();
        e.score = outbox.getScore();
        e.status = outbox.getStatus();
        e.retryCount  = outbox.getRetryCount();
        e.processedAt = outbox.getProcessedAt();
        return e;
    }
    public WaitingOutbox toDomain() {
        return WaitingOutbox.restore(
                outboxId,
                waitingId,
                token,
                restaurantId,
                score,
                status,
                retryCount,
                processedAt
        );
    }
}
