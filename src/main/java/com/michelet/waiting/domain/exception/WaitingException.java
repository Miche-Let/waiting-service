package com.michelet.waiting.domain.exception;

import com.michelet.common.exception.BusinessException;

public class WaitingException extends BusinessException {
    public WaitingException(WaitingErrorCode errorCode){
        super(errorCode);
    }
}
