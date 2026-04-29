package com.michelet.waiting.infrastructure.messaging.consumer;

import com.michelet.waiting.application.port.WaitingEventConsumer;
import com.michelet.waiting.application.service.WaitingService;
import com.michelet.waiting.domain.event.WaitingActivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaWaitingEventConsumer implements WaitingEventConsumer {

    private final WaitingService waitingService;

    // 예약 서비스가 예약 완료 시 발행 -> 대기 종료 처리
    @KafkaListener(
            topics = "waiting.activated",
            groupId = "waiting-service"
    )

    @Override
    public void onWaitingActivated(WaitingActivatedEvent event) {
        log.info("Kafka 수신 - waitingId: {}", event.waitingId());
        waitingService.handleActivated(event);
    }
}
