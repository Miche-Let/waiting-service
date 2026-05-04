package com.michelet.waiting.infrastructure.persistence.querydsl;

import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.infrastructure.persistence.jpa.QWaitingJpaEntity;
import com.michelet.waiting.infrastructure.persistence.jpa.WaitingJpaEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WaitingQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QWaitingJpaEntity w = QWaitingJpaEntity.waitingJpaEntity;

    // ACTIVE 상태 중 activatedAt 기준 만료 대상 조회
    public List<WaitingJpaEntity> findExpiredActives(LocalDateTime expiredBefore){
        Objects.requireNonNull(expiredBefore, "expiredBefore must not be null");
        return queryFactory
                .selectFrom(w)
                .where(
                        w.status.eq(WaitingStatus.ACTIVE),
                        w.activatedAt.before(expiredBefore),
                        w._super.deletedAt.isNull()
                )
                .fetch();
    }

    public List<UUID> findDistinctRestaurantIdsWithWaiting(){
        return queryFactory
                .select(w.restaurantId)
                .distinct()
                .from(w)
                .where(
                        w.status.eq(WaitingStatus.WAITING),
                        w._super.deletedAt.isNull()
                )
                .fetch();
    }
}
