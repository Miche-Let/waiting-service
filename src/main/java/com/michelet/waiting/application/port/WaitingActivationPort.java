package com.michelet.waiting.application.port;

import java.util.List;
import java.util.UUID;

public interface WaitingActivationPort {

    // 대기열 등록 + 순번 반환 - enterWaiting(),  에서 호출
    // 현재 순번 조회 - getStatus() 에서 호출
    Long addIfAbsentAndGetPosition(UUID restaurantId, String token);

    // 전체 대기 인원 수 조회 (Redis ZCARD)
    Long countWaiting(UUID restaurantId);

    // 앞에서 N개 토큰 꺼내기 - 스케줄러 activateNextBatch() 에서 호출
    List<String> popNextTokens(UUID restaurantId, int count);

    // 대기열에서 제거 - 취소/만료 시 호출
    void remove(UUID restaurantId, String token);

}
