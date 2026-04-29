package com.michelet.waiting.domain.exception;

import com.michelet.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WaitingErrorCode implements ErrorCode{
    NOT_FOUND("WAITING_001", "대기열을 찾을 수 없습니다.", 404),
    INVALID_STATE("WAITING_002", "대기 상태가 유효하지 않습니다.", 400),
    INVALID_TOKEN("WAITING_003", "유효하지 않은 토큰입니다.", 400),
    ALREADY_IN("WAITING_004", "이미 대기열에 등록되어 있습니다.", 409);

    private final String code;
    private final String message;
    private final int httpStatus;

}
