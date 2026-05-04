package com.michelet.waiting.presentation.dto.request;


import com.michelet.common.auth.webmvc.context.UserContextHolder;
import com.michelet.waiting.application.dto.EnterWaitingCommand;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnterWaitingRequest (
        @NotNull UUID restaurantId
    ){
    public EnterWaitingCommand toCommand(){
        UUID userId = UUID.fromString(UserContextHolder.get().userId());
        return new EnterWaitingCommand(userId, restaurantId);
    }
}
