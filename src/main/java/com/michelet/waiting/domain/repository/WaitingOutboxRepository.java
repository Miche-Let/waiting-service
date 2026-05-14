package com.michelet.waiting.domain.repository;

import com.michelet.waiting.domain.entity.WaitingOutbox;
import java.util.List;

public interface WaitingOutboxRepository {
    void save(WaitingOutbox outbox);
    List<WaitingOutbox> findPendingOrFailed();
    void update(WaitingOutbox outbox);
}
