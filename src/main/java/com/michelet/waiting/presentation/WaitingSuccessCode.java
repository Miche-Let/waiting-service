package com.michelet.waiting.presentation;

import com.michelet.common.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WaitingSuccessCode implements SuccessCode {

    ENTER_SUCCESS("WAITING_001","대기열 등록이 완료되었습니다.",201),
    GET_SUCCESS("WAITING_002", "대기 상태 조회가 완료되었습니다.",200),
    CANCEL_SUCCESS("WAITING_003", "대기열 취소가 완료되었습니다.",200),
    DELETE_SUCCESS("WAITING_004", "대기열이 삭제되었습니다.",200);

    private final String code;
    private final String message;
    private final int httpStatus;

}
