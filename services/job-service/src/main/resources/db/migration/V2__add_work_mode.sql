-- V2: Expand work_mode - already in V1 as ENUM; add index for composite filters
-- Expand-and-contract: safe additive migration

ALTER TABLE jobs ADD COLUMN IF NOT EXISTS work_mode_notes VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_jobs_work_mode_status ON jobs (work_mode, status);

COMMENT ON COLUMN jobs.work_mode_notes IS 'Optional notes about hybrid/remote arrangements';
