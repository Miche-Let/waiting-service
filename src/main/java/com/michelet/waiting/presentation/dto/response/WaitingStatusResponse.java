package com.michelet.waiting.presentation.dto.response;

import com.michelet.waiting.application.dto.WaitingResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record WaitingStatusResponse(
        UUID waitingId,
        String token,
        Long position,
        String status,
        LocalDateTime enteredAt,
        LocalDateTime activatedAt,
        Long estimatedWaitSeconds
) {
    public static WaitingStatusResponse from(WaitingResult result){
        return new WaitingStatusResponse(
                result.id(),
                result.token(),
                result.position(),
                result.status().name(),
                result.enteredAt(),
                result.activatedAt(),
                result.estimatedWaitSeconds()
        );
    }
}
