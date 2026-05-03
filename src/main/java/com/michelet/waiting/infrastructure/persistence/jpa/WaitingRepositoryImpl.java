package com.michelet.waiting.infrastructure.persistence.jpa;

import com.michelet.waiting.domain.entity.Waiting;
import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.domain.repository.WaitingRepository;
import com.michelet.waiting.infrastructure.persistence.querydsl.WaitingQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WaitingRepositoryImpl implements WaitingRepository {

    private final WaitingJpaRepository jpa;
    private final WaitingQueryRepository queryRepository;

    @Override
    public Waiting save(Waiting waiting) {
        return jpa.save(WaitingJpaEntity.from(waiting)).toDomain();
    }

    @Override
    public Optional<Waiting> findByToken(String token) {
        return jpa.findByTokenAndDeletedAtIsNull(token)
                .map(WaitingJpaEntity::toDomain);
    }

    @Override
    public Optional<Waiting> findById(UUID id) {
        return jpa.findByIdAndDeletedAtIsNull(id)
                .map(WaitingJpaEntity::toDomain);
    }

    @Override
    public List<Waiting> findWaitingByRestaurantId(UUID restaurantId) {
        return jpa.findByRestaurantIdAndStatusAndDeletedAtIsNull(restaurantId, WaitingStatus.WAITING)
                .stream()
                .map(WaitingJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Waiting> findExpiredActives(LocalDateTime expiredBefore) {
        return queryRepository.findExpiredActives(expiredBefore)
                .stream()
                .map(WaitingJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void softDelete(UUID waitingId, UUID deletedBy) {
        jpa.findByIdAndDeletedAtIsNull(waitingId)
                .ifPresent(entity -> {
                    entity.softDelete(deletedBy);
                    jpa.save(entity);
                });
    }

    @Override
    public List<UUID> findDistinctRestaurantIdsWithWaiting() {
        return queryRepository.findDistinctRestaurantIdsWithWaiting();
    }

    @Override
    public void deleteExpiredBefore(LocalDateTime threshold) {
        jpa.deleteByStatusAndEnteredAtBefore(WaitingStatus.EXPIRED, threshold);
    }
}
