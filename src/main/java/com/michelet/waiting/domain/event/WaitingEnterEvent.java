package com.michelet.waiting.domain.event;

import java.util.UUID;

public record WaitingEnterEvent(
        UUID waitingId,
        UUID userId,
        UUID restaurantId
) {
}

