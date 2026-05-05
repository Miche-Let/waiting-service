package com.michelet.waiting.domain.repository;

import com.michelet.waiting.domain.entity.Waiting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaitingRepository {

    Waiting save(Waiting waiting);
    Optional<Waiting> findByToken(String token);
    Optional<Waiting> findById(UUID id);
    Optional<Waiting> findByAccessToken(String accessToken);
    List<Waiting> findWaitingByRestaurantId(UUID restaurantId);
    List<Waiting> findExpiredActives(LocalDateTime expiredBefore);
    List<UUID> findDistinctRestaurantIdsWithWaiting();
    void softDelete(UUID waitingId, UUID deletedBy);

}
