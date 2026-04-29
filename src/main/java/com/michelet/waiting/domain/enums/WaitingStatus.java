package com.michelet.waiting.domain.enums;

public enum WaitingStatus {
    WAITING, // 대기중 - redis sorted set에 존재
    ACTIVE, // 진입 가능 - redis에서 제거, 예약 진행 가능
    EXPIRED, // 10분 초과 만료
    CANCELLED // 사용자 직접 취소
}
