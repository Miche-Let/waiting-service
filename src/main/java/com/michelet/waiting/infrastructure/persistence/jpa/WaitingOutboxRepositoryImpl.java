package com.michelet.waiting.infrastructure.persistence.jpa;

import com.michelet.waiting.domain.entity.WaitingOutbox;
import com.michelet.waiting.domain.enums.OutboxStatus;
import com.michelet.waiting.domain.repository.WaitingOutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WaitingOutboxRepositoryImpl implements WaitingOutboxRepository {

    private final WaitingOutboxJpaRepository jpa;

    @Override
    public void save(WaitingOutbox outbox) {
        jpa.save(WaitingOutboxJpaEntity.from(outbox));
    }

    @Override
    public List<WaitingOutbox> findPending() {
        return jpa.findByStatus(OutboxStatus.PENDING)
                .stream()
                .map(WaitingOutboxJpaEntity::toDomain)
                .toList();

    }

    @Override
    public void update(WaitingOutbox outbox) {
        jpa.save(WaitingOutboxJpaEntity.from(outbox));
    }
}
