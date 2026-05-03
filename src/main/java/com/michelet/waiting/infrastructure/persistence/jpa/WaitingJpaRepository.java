package com.michelet.waiting.infrastructure.persistence.jpa;

import com.michelet.waiting.domain.enums.WaitingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaitingJpaRepository extends JpaRepository<WaitingJpaEntity, UUID> {

    Optional<WaitingJpaEntity> findByTokenAndDeletedAtIsNull (String token);
    Optional<WaitingJpaEntity> findByIdAndDeletedAtIsNull(UUID id);
    Optional<WaitingJpaEntity> findByAccessTokenAndDeletedAtIsNull(String accessToken);
    List<WaitingJpaEntity> findByRestaurantIdAndStatusAndDeletedAtIsNull(
            UUID restaurantId, WaitingStatus status
    );
}
