package com.michelet.common.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * common 모듈의 BaseEntity에 대한 QueryDSL 타입
 *
 * QBaseEntity는 restaurant-service에서 직접 관리
 * QueryDSL을 사용하는 서비스 레포에서 필요한 Q 타입을 각 서비스가 독립적으로 보유하는 구조
 */
public class QBaseEntity extends EntityPathBase<BaseEntity> {

    private static final long serialVersionUID = 1L;

    public static final QBaseEntity baseEntity = new QBaseEntity("baseEntity");

    public final DateTimePath<LocalDateTime> createdAt =
            createDateTime("createdAt", LocalDateTime.class);

    public final ComparablePath<UUID> createdBy =
            createComparable("createdBy", UUID.class);

    public final DateTimePath<LocalDateTime> updatedAt =
            createDateTime("updatedAt", LocalDateTime.class);

    public final ComparablePath<UUID> updatedBy =
            createComparable("updatedBy", UUID.class);

    public final DateTimePath<LocalDateTime> deletedAt =
            createDateTime("deletedAt", LocalDateTime.class);

    public final ComparablePath<UUID> deletedBy =
            createComparable("deletedBy", UUID.class);

    public QBaseEntity(String variable) {
        super(BaseEntity.class, forVariable(variable));
    }

    public QBaseEntity(Path<? extends BaseEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseEntity(PathMetadata metadata) {
        super(BaseEntity.class, metadata);
    }
}