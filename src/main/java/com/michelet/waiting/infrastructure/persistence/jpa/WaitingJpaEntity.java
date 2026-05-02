package com.michelet.waiting.infrastructure.persistence.jpa;

import com.michelet.common.entity.BaseEntity;
import com.michelet.waiting.domain.entity.Waiting;
import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.domain.vo.WaitingToken;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Table(name = "p_waiting_queue")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WaitingJpaEntity extends BaseEntity {

    @Id
    @Column(name ="waiting_id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "restaurant_id", nullable = false, columnDefinition = "uuid")
    private UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WaitingStatus status;

    @Column(name = "queue_token", nullable = false, unique = true)
    private String token;

    @Column(name = "entered_at", nullable = false, updatable = false)
    private LocalDateTime enteredAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    // Domain -> JPA
    public static WaitingJpaEntity from(Waiting w){
        WaitingJpaEntity e = new WaitingJpaEntity();
        e.id = w.getId();
        e.userId = w.getUserId();
        e.restaurantId = w.getRestaurantId();
        e.status = w.getStatus();
        e.token = w.getToken().value();
        e.enteredAt = w.getEnteredAt();
        e.activatedAt = w.getActivatedAt();
        return e;
    }
    // JPA -> Domain
    public Waiting toDomain() {
        return Waiting.restore(
                id, userId, restaurantId,
                WaitingToken.of(token),
                status, enteredAt, activatedAt
        );
    }


}
