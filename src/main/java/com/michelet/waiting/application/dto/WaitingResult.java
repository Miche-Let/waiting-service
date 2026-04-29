package com.michelet.waiting.application.dto;

import com.michelet.waiting.domain.entity.Waiting;
import com.michelet.waiting.domain.enums.WaitingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WaitingResult(
        UUID id,
        String token,
        Long position,
        WaitingStatus status,
        LocalDateTime enteredAt,
        LocalDateTime activatedAt,
        Long estimatedWaitSeconds
) {
    // 대기중 - position 필요
    public static WaitingResult of(Waiting w, Long position){
        return new WaitingResult(
                w.getId(),
                w.getToken().value(),
                position,
                w.getStatus(),
                w.getEnteredAt(),
                w.getActivatedAt(),
                position * 30L
        );
    }

    public static WaitingResult of(Waiting w){
        return new WaitingResult(
                w.getId(),
                w.getToken().value(),
                0L,
                w.getStatus(),
                w.getEnteredAt(),
                w.getActivatedAt(),
                0L
        );
    }
}
