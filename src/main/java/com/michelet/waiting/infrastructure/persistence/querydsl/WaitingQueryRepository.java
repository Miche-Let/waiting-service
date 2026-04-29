package com.michelet.waiting.infrastructure.persistence.querydsl;

import com.michelet.waiting.domain.enums.WaitingStatus;
import com.michelet.waiting.infrastructure.persistence.jpa.QWaitingJpaEnitty;
import com.michelet.waiting.infrastructure.persistence.jpa.WaitingJpaEnitty;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class WaitingQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QWaitingJpaEnitty w = QWaitingJpaEnitty.waitingJpaEnitty;

    // ACTIVE 상태 중 activatedAt 기준 만료 대상 조회
    public List<WaitingJpaEnitty> findExpiredActives(LocalDateTime expiredBefore){
        return queryFactory
                .selectFrom(w)
                .where(
                        w.status.eq(WaitingStatus.ACTIVE),
                        w.activatedAt.before(expiredBefore)
                )
                .fetch();
    }
}
