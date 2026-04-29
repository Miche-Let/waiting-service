package com.michelet.waiting.domain.event;

import java.util.UUID;

public record WaitingActivatedEvent (
        UUID waitingId,
        UUID userId,
        UUID restaurantId){

}
