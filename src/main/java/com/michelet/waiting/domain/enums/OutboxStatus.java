package com.michelet.waiting.domain.enums;

public enum OutboxStatus {
    PENDING,    // 처리 시도 중
    PROCESSED,  // 처리 완료
    FAILED,      // 처리 실패(재처리 대상)
    ABANDONED   // N회 실패 후 포기 상태
}
