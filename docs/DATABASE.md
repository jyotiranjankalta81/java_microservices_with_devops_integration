# Database Design & Migration Strategy

## Layman's Explanation

The database is organized like a well-labeled filing system. Jobs link to companies, locations, skills, and technologies through separate tables so we don't repeat the same company name 500 times. Flyway migrations are **versioned recipe cards** — each change is numbered, reviewed, and applied in order, never by letting Hibernate guess the schema at startup.

## Table Overview

| Table | Purpose |
|-------|---------|
| `jobs` | Core job record with FK to company, location, source |
| `companies` | Normalized company names (unique) |
| `locations` | Country/state/city (indexed for geo filters) |
| `skills` / `technologies` | Tag dictionaries |
| `job_skills` / `job_technologies` | Many-to-many join tables |
| `job_tags` | Free-form tags |
| `job_sources` | LinkedIn, Indeed, referral, etc. |
| `outbox_events` | Transactional outbox for Kafka |

## Important Indexes

| Index | Why |
|-------|-----|
| `idx_jobs_user_status_created` | List user's jobs sorted by date — most common query |
| `idx_jobs_company_id` | Filter by company |
| `idx_locations_country_city` | Filter by country + city together |
| `idx_job_skills_skill_id` | Reverse lookup: all jobs needing Java |
| `idx_outbox_pending` | Partial index — only unpublished events |
| `uq_jobs_user_url` | Prevent duplicate job URLs per user |

## Migration Patterns

### Expand-and-Contract
1. **Expand**: Add nullable column (`salary_min` in V3)
2. **Migrate**: Backfill data in application or batch job
3. **Contract**: Add NOT NULL constraint in later migration

### Adding Nullable Columns
Safe for zero-downtime — old app versions ignore new columns.

### Adding NOT NULL Safely
1. Add column nullable
2. Backfill default values
3. Add NOT NULL constraint
4. Remove default if needed

### Adding Indexes Safely
Use `CREATE INDEX CONCURRENTLY` in production (Flyway supports via separate migration with `executeInTransaction=false`).

### Column Renaming
1. Add new column
2. Dual-write in application
3. Backfill
4. Switch reads
5. Drop old column

### Rollback Strategy
- Flyway `undo` (Teams) or forward-fix migration
- Never delete data when adding columns
- Keep migrations idempotent where possible (`IF NOT EXISTS`)

## Optimistic Locking

`jobs.version` column — JPA `@Version` prevents lost updates on concurrent edits. Conflict → 409 to client.
