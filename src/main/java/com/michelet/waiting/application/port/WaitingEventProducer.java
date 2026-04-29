package com.michelet.waiting.application.port;


import com.michelet.waiting.domain.event.WaitingActivatedEvent;

public interface WaitingEventProducer {

    // 입장 허용 시 발행 - 알림 서비스가 구독해서 입장가능 알림 발송
    void publish(WaitingActivatedEvent event);
}
