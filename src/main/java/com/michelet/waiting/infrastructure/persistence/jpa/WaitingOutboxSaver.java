package com.michelet.waiting.infrastructure.persistence.jpa;

import com.michelet.waiting.domain.entity.WaitingOutbox;
import com.michelet.waiting.domain.repository.WaitingOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WaitingOutboxSaver {

    private final WaitingOutboxRepository waitingOutboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(WaitingOutbox outbox) {
        waitingOutboxRepository.save(outbox);
    }
}