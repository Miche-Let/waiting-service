package com.michelet.waiting.application.port;

import com.michelet.waiting.domain.event.WaitingActivatedEvent;

public interface WaitingEventConsumer {

    // 외부 서비스에서 발행한 이벤트 수신
    // 예약 서비스가 예약 완료 시 대기 상태를 종료 시킬 때
    void onWaitingActivated(WaitingActivatedEvent event);
}
