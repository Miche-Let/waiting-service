package com.michelet.waiting.domain.entity;

import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;
import com.michelet.waiting.domain.vo.WaitingToken;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Waiting {
    private final UUID id;
    private final UUID userId;
    private final UUID restaurantId;
    private final WaitingToken token;
    private WaitingStatus status;
    private final LocalDateTime enteredAt;

    private Waiting(UUID id, UUID userId, UUID restaurantId, WaitingToken token,
                    WaitingStatus status, LocalDateTime enteredAt){
        this.id = id;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.token = token;
        this.status = status;
        this.enteredAt = enteredAt;
    }

    public static Waiting create(UUID userId, UUID restaurantId){
        return new Waiting(
                UUID.randomUUID(), userId, restaurantId,
                WaitingToken.generate(),
                WaitingStatus.WAITING,
                LocalDateTime.now()
        );
    }

    // db 복원용 , JpaEntity.toDomain() 에서만 호출용으로 사용
    public static Waiting restore(UUID id, UUID userId, UUID restaurantId,
                                  WaitingToken token, WaitingStatus status,
                                  LocalDateTime enteredAt){
        return new Waiting(id, userId, restaurantId, token, status, enteredAt);
    }

    public void activate(){
        if(status != WaitingStatus.WAITING)
            throw new WaitingException(WaitingErrorCode.INVALID_STATE);
        this.status = WaitingStatus.ACTIVE;
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
        return enteredAt.isBefore(LocalDateTime.now().minusMinutes(10));
    }
    public boolean isActive(){
        return status == WaitingStatus.ACTIVE;
    }
}
