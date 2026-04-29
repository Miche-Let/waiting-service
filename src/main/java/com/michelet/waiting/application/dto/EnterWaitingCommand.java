package com.michelet.waiting.application.dto;

import java.util.UUID;

public record EnterWaitingCommand(
        UUID userId,
        UUID restaurantId
) {

}
