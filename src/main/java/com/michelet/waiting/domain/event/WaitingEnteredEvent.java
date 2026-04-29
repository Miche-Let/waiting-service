package com.michelet.waiting.domain.event;

import java.util.UUID;

public record WaitingEnteredEvent(
        UUID waitingId,
        UUID userId,
        UUID restaurantId
) {
}

