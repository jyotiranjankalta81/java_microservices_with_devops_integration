-- V3: Add salary columns (nullable first for zero-downtime expand phase)
-- NOT NULL can be enforced in a later contract phase after backfill

ALTER TABLE jobs ADD COLUMN salary_min NUMERIC(15, 2);
ALTER TABLE jobs ADD COLUMN salary_max NUMERIC(15, 2);
ALTER TABLE jobs ADD COLUMN salary_currency VARCHAR(3) DEFAULT 'USD';

ALTER TABLE jobs ADD CONSTRAINT chk_salary_range CHECK (
    salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max
);

CREATE INDEX idx_jobs_salary_range ON jobs (salary_min, salary_max) WHERE salary_min IS NOT NULL;
