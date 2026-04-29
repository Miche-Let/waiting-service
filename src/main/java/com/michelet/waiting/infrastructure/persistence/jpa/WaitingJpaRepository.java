package com.michelet.waiting.infrastructure.persistence.jpa;

import com.michelet.waiting.domain.enums.WaitingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaitingJpaRepository extends JpaRepository<WaitingJpaEntity, UUID> {

    Optional<WaitingJpaEntity> findByToken(String token);

    List<WaitingJpaEntity> findByRestaurantIdAndStatus(
            UUID restaurantId, WaitingStatus status
    );

    void deleteByStatusAndEnteredAtBefore(
            WaitingStatus status, LocalDateTime threshold
    );
}
