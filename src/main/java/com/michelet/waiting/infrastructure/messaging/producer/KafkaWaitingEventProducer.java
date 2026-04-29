package com.michelet.waiting.infrastructure.messaging.producer;

import com.michelet.waiting.application.port.WaitingEventProducer;
import com.michelet.waiting.domain.event.WaitingActivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaWaitingEventProducer implements WaitingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_ACTIVATED = "waiting.activated";

    @Override
    public void publish(WaitingActivatedEvent event) {
        // key = waitingId , 같은 대기열 이벤트는 같은 파티션으로
        kafkaTemplate.send(TOPIC_ACTIVATED, event.waitingId().toString(), event)
                .whenComplete((result, ex) -> {
                    if(ex != null){
                        log.error("Kafka 발행 실패 - topic: {}, waitingId: {}",
                                TOPIC_ACTIVATED, event.waitingId(), ex);
                    }else{
                        log.info("Kafka 발행 성공 - topic: {}, waitingId: {}",
                                TOPIC_ACTIVATED, event.waitingId());
                    }
                });
    }
}
