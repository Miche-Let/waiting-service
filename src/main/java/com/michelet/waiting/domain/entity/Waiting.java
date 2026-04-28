package com.michelet.waiting.domain.entity;

import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;
import com.michelet.waiting.domain.vo.WaitingToken;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Waiting {
    private final UUID id;
    private final UUID userId;
    private final UUID restaurantId;
    private final WaitingToken token;
    private WaitingStatus status;
    private final LocalDateTime enteredAt;
    private       LocalDateTime activatedAt;

    private Waiting(UUID id, UUID userId, UUID restaurantId, WaitingToken token,
                    WaitingStatus status, LocalDateTime enteredAt, LocalDateTime activatedAt){
        this.id           = Objects.requireNonNull(id,           "id must not be null");
        this.userId       = Objects.requireNonNull(userId,       "userId must not be null");
        this.restaurantId = Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        this.token        = Objects.requireNonNull(token,        "token must not be null");
        this.status       = Objects.requireNonNull(status,       "status must not be null");
        this.enteredAt    = Objects.requireNonNull(enteredAt,    "enteredAt must not be null");
        this.activatedAt  = activatedAt;
    }

    public static Waiting create(UUID userId, UUID restaurantId){
        return new Waiting(
                UUID.randomUUID(), userId, restaurantId,
                WaitingToken.generate(),
                WaitingStatus.WAITING,
                LocalDateTime.now(),
                null
        );
    }

    // db 복원용 , JpaEntity.toDomain() 에서만 호출용으로 사용
    public static Waiting restore(UUID id, UUID userId, UUID restaurantId,
                                  WaitingToken token, WaitingStatus status,
                                  LocalDateTime enteredAt, LocalDateTime activatedAt){
        return new Waiting(id, userId, restaurantId, token, status, enteredAt, activatedAt);
    }

    public void activate(){
        if (status != WaitingStatus.WAITING)
            throw new WaitingException(WaitingErrorCode.INVALID_STATE);
        this.status      = WaitingStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
    }

    public void cancel(){
        if(status != WaitingStatus.WAITING)
            throw new WaitingException(WaitingErrorCode.INVALID_STATE);
        this.status = WaitingStatus.CANCELLED;
    }

    public void expire() {
        if(status != WaitingStatus.WAITING)
            throw new WaitingException(WaitingErrorCode.INVALID_STATE);
        this.status = WaitingStatus.EXPIRED;
    }

    public boolean isExpired(){
        if (status != WaitingStatus.ACTIVE) return false;
        return activatedAt.isBefore(LocalDateTime.now().minusMinutes(10));
    }
    public boolean isActive(){
        return status == WaitingStatus.ACTIVE;
    }
}
