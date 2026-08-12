-- V1: Initial normalized schema for job tracking
-- Index rationale documented inline.

CREATE TABLE companies (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_companies_name UNIQUE (name)
);
CREATE INDEX idx_companies_name ON companies (name);

CREATE TABLE locations (
    id          BIGSERIAL PRIMARY KEY,
    country     VARCHAR(100) NOT NULL,
    state       VARCHAR(100),
    city        VARCHAR(100) NOT NULL,
    location_text VARCHAR(500),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_locations_country_city ON locations (country, city);
CREATE INDEX idx_locations_city ON locations (city);

CREATE TABLE job_sources (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    url         VARCHAR(500),
    CONSTRAINT uq_job_sources_name UNIQUE (name)
);

CREATE TABLE skills (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    CONSTRAINT uq_skills_name UNIQUE (name)
);

CREATE TABLE technologies (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    CONSTRAINT uq_technologies_name UNIQUE (name)
);

CREATE TABLE jobs (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             UUID NOT NULL,
    title               VARCHAR(255) NOT NULL,
    company_id          BIGINT NOT NULL REFERENCES companies(id),
    role                VARCHAR(255) NOT NULL,
    description         TEXT,
    location_id         BIGINT REFERENCES locations(id),
    work_mode           VARCHAR(20) NOT NULL DEFAULT 'REMOTE',
    experience_min      INTEGER,
    experience_max      INTEGER,
    employment_type     VARCHAR(30) NOT NULL DEFAULT 'FULL_TIME',
    job_source_id       BIGINT REFERENCES job_sources(id),
    job_url             VARCHAR(1000),
    posted_date         DATE,
    application_deadline DATE,
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    notes               TEXT,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_experience_range CHECK (
        experience_min IS NULL OR experience_max IS NULL OR experience_min <= experience_max
    )
);

-- Primary query patterns: user jobs, filter by company/role/status, sort by dates
CREATE INDEX idx_jobs_user_id ON jobs (user_id);
CREATE INDEX idx_jobs_company_id ON jobs (company_id);
CREATE INDEX idx_jobs_role ON jobs (role);
CREATE INDEX idx_jobs_status ON jobs (status);
CREATE INDEX idx_jobs_work_mode ON jobs (work_mode);
CREATE INDEX idx_jobs_posted_date ON jobs (posted_date);
CREATE INDEX idx_jobs_created_at ON jobs (created_at);
CREATE INDEX idx_jobs_user_status_created ON jobs (user_id, status, created_at DESC);

CREATE TABLE job_skills (
    job_id      BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill_id    BIGINT NOT NULL REFERENCES skills(id),
    PRIMARY KEY (job_id, skill_id)
);
CREATE INDEX idx_job_skills_skill_id ON job_skills (skill_id);

CREATE TABLE job_technologies (
    job_id          BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    technology_id   BIGINT NOT NULL REFERENCES technologies(id),
    PRIMARY KEY (job_id, technology_id)
);
CREATE INDEX idx_job_technologies_technology_id ON job_technologies (technology_id);

CREATE TABLE job_tags (
    job_id      BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    tag         VARCHAR(100) NOT NULL,
    PRIMARY KEY (job_id, tag)
);
CREATE INDEX idx_job_tags_tag ON job_tags (tag);

CREATE TABLE outbox_events (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMPTZ,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);
CREATE INDEX idx_outbox_pending ON outbox_events (status, created_at) WHERE status = 'PENDING';
