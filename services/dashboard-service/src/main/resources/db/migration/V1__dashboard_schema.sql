CREATE TABLE dashboard_summary (
    user_id         UUID PRIMARY KEY,
    total_jobs      BIGINT NOT NULL DEFAULT 0,
    jobs_today      BIGINT NOT NULL DEFAULT 0,
    jobs_this_week  BIGINT NOT NULL DEFAULT 0,
    jobs_this_month BIGINT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE dimension_counts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID NOT NULL,
    dimension   VARCHAR(50) NOT NULL,
    dimension_value VARCHAR(255) NOT NULL,
    count       BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_dimension UNIQUE (user_id, dimension, dimension_value)
);
CREATE INDEX idx_dimension_user_dim ON dimension_counts (user_id, dimension);

CREATE TABLE processed_events (
    event_id    UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
