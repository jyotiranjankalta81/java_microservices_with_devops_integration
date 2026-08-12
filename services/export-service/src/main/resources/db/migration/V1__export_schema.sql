CREATE TABLE export_jobs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    filter_json TEXT,
    file_path   VARCHAR(500),
    error_message TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);
CREATE INDEX idx_export_jobs_user ON export_jobs (user_id, created_at DESC);
