package com.michelet.waiting.presentation.dto.request;


import com.michelet.common.auth.webmvc.context.UserContextHolder;
import com.michelet.waiting.application.dto.EnterWaitingCommand;
import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnterWaitingRequest (
        @NotNull UUID restaurantId
    ){
    public EnterWaitingCommand toCommand(){
        try{
            UUID userId = UUID.fromString(UserContextHolder.get().userId());
            return new EnterWaitingCommand(userId, restaurantId);
        }catch (IllegalArgumentException | NullPointerException e){
            throw new WaitingException(WaitingErrorCode.UNAUTHORIZED);
        }
    }
}
