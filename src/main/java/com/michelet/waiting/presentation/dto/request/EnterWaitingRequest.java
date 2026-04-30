package com.michelet.waiting.presentation.dto.request;


import com.michelet.waiting.application.dto.EnterWaitingCommand;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnterWaitingRequest (
        @NotNull UUID userId,
        @NotNull UUID restaurantId
    ){
    public EnterWaitingCommand toCommand(){
        return new EnterWaitingCommand(userId, restaurantId);
    }
}
