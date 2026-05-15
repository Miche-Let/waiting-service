CREATE TABLE IF NOT EXISTS waiting_service.p_waiting_queue
(
    waiting_id    UUID         NOT NULL,
    user_id       UUID         NOT NULL,
    restaurant_id UUID         NOT NULL,
    status        VARCHAR(20)  NOT NULL
        CHECK (status IN ('WAITING', 'ACTIVE', 'EXPIRED', 'CANCELLED')),
    queue_token   VARCHAR(255) NOT NULL,
    entered_at    TIMESTAMP    NOT NULL,
    activated_at  TIMESTAMP,
    access_token  VARCHAR(255) UNIQUE,

    -- BaseEntity 컬럼
    created_at    TIMESTAMP    NOT NULL,
    created_by    UUID,
    updated_at    TIMESTAMP,
    updated_by    UUID,
    deleted_at    TIMESTAMP,
    deleted_by    UUID,

    PRIMARY KEY (waiting_id),

    CONSTRAINT uq_queue_token UNIQUE (queue_token)
);
CREATE TABLE IF NOT EXISTS waiting_service.p_waiting_outbox
(
    outbox_id     UUID         NOT NULL,
    waiting_id    UUID,
    token         VARCHAR(255) NOT NULL,
    restaurant_id UUID         NOT NULL,
    score         BIGINT       NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED')),
    retry_count   INT          NOT NULL DEFAULT 0,
    processed_at  TIMESTAMP,
    created_at    TIMESTAMP    NOT NULL,
    created_by    UUID,
    updated_at    TIMESTAMP,
    updated_by    UUID,
    deleted_at    TIMESTAMP,
    deleted_by    UUID,

    PRIMARY KEY (outbox_id)
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_waiting_active_unique
    ON waiting_service.p_waiting_queue (user_id, restaurant_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_waiting_restaurant_status
    ON waiting_service.p_waiting_queue (restaurant_id, status);

CREATE INDEX IF NOT EXISTS idx_waiting_token
    ON waiting_service.p_waiting_queue (queue_token);

CREATE INDEX IF NOT EXISTS idx_outbox_status
    ON waiting_service.p_waiting_outbox (status);
