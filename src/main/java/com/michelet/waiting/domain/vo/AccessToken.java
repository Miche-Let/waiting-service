package com.michelet.waiting.domain.vo;

import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;

import java.util.UUID;

public record AccessToken(String value) {

    public AccessToken{
        if(value == null || value.isBlank())
            throw new WaitingException(WaitingErrorCode.INVALID_TOKEN);
    }
    public static AccessToken generate(){
        return new AccessToken(UUID.randomUUID().toString());
    }
    public static AccessToken of(String value){
        return new AccessToken(value);
    }
}
