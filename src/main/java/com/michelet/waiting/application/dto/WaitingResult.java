package com.michelet.waiting.application.dto;

import com.michelet.waiting.domain.entity.Waiting;
import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record WaitingResult(
        UUID id,
        String token,
        Long position,
        WaitingStatus status,
        LocalDateTime enteredAt,
        LocalDateTime activatedAt,
        Long estimatedWaitSeconds,
        String accessToken
) {
    // WAITING 상태 - position 필요
    public static WaitingResult of(Waiting w, Long position){
        Objects.requireNonNull(w, "waiting must not be null");
        Objects.requireNonNull(position, "position must not be null");
        if(position < 0) throw new WaitingException(WaitingErrorCode.INVALID_POSITION);

        return new WaitingResult(
                w.getId(),
                w.getToken().value(),
                position,
                w.getStatus(),
                w.getEnteredAt(),
                w.getActivatedAt(),
                Math.multiplyExact(position, 30L),
                null
        );
    }

    // ACTIVE / EXPIRED / CANCELLED 상태
    public static WaitingResult of(Waiting w){
        Objects.requireNonNull(w, "waiting must not be null");
        return new WaitingResult(
                w.getId(),
                w.getToken().value(),
                0L,
                w.getStatus(),
                w.getEnteredAt(),
                w.getActivatedAt(),
                0L,
                w.getAccessToken() != null
                    ? w.getAccessToken().value()
                    :null
        );
    }
}
