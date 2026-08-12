-- V4: Enrich job source tracking - backward compatible
ALTER TABLE job_sources ADD COLUMN IF NOT EXISTS source_type VARCHAR(50) DEFAULT 'OTHER';
ALTER TABLE job_sources ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX IF NOT EXISTS idx_job_sources_type ON job_sources (source_type);

-- Duplicate prevention: same user + same job URL (when URL present)
CREATE UNIQUE INDEX IF NOT EXISTS uq_jobs_user_url
    ON jobs (user_id, job_url)
    WHERE job_url IS NOT NULL AND job_url <> '';
