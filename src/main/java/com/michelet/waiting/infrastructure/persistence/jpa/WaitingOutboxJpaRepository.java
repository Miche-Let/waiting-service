package com.michelet.waiting.infrastructure.persistence.jpa;

import com.michelet.waiting.domain.enums.OutboxStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitingOutboxJpaRepository extends JpaRepository<WaitingOutboxJpaEntity, UUID> {
    List<WaitingOutboxJpaEntity> findByStatusIn(List<OutboxStatus> statuses);

}
