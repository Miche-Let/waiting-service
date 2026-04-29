package com.michelet.waiting.domain.vo;

import com.michelet.waiting.domain.exception.WaitingErrorCode;
import com.michelet.waiting.domain.exception.WaitingException;

import java.util.UUID;

public record WaitingToken (String value){
    public WaitingToken{
        if(value == null || value.isBlank()){
            throw new WaitingException(WaitingErrorCode.INVALID_TOKEN);
        }
    }
    public static WaitingToken generate() {
        return new WaitingToken(UUID.randomUUID().toString());
    }
    public static WaitingToken of(String value){
        return new WaitingToken(value);
    }
}
