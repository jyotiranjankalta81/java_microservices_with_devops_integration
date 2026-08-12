# Job Intelligence Platform — Technical Details

Complete technical reference for the enterprise Job Tracker platform. Written for developers preparing for **5+ years Java/Spring Boot** interviews — each section starts with a plain-language explanation, then covers implementation details, production considerations, and interview talking points.

**Related docs:**
- [ARCHITECTURE.md](ARCHITECTURE.md) — service boundaries and communication patterns
- [DATABASE.md](DATABASE.md) — schema, indexes, Flyway migration strategy
- [FAILURE_SCENARIOS.md](FAILURE_SCENARIOS.md) — 25 production failure walkthroughs

---

## Table of Contents

0. [Business Requirements](#0-business-requirements)
1. [Technology Stack](#1-technology-stack)
2. [Project Structure](#2-project-structure)
3. [Phase 1 — Project Architecture](#3-phase-1--project-architecture)
4. [Phase 2 — Database Design](#4-phase-2--database-design)
5. [Phase 3–5 — Job Service](#5-phase-35--job-service)
6. [Phase 6 — React Frontend](#6-phase-6--react-frontend)
7. [Phase 7 — Dashboard Service (CQRS)](#7-phase-7--dashboard-service-cqrs)
8. [Phase 8 — Export Service](#8-phase-8--export-service)
9. [Phase 9 — API Gateway](#9-phase-9--api-gateway)
10. [Phase 10 — Authentication & Authorization](#10-phase-10--authentication--authorization)
11. [Phase 11–12 — Kafka & Outbox Pattern](#11-phase-1112--kafka--outbox-pattern)
12. [Phase 13–14 — Dashboard Events & Audit](#12-phase-1314--dashboard-events--audit)
13. [Phase 15 — Notification Service](#13-phase-15--notification-service)
14. [Phase 16 — Observability](#14-phase-16--observability)
15. [Phase 17 — Resilience](#15-phase-17--resilience)
16. [Phase 18 — Testing](#16-phase-18--testing)
17. [Phase 19–20 — Docker & Production Configuration](#17-phase-1920--docker--production-configuration)
18. [Transaction Management](#18-transaction-management)
19. [Connection Pooling (HikariCP)](#19-connection-pooling-hikaricp)
20. [Exception Handling](#20-exception-handling)
21. [OpenAPI & API Documentation](#21-openapi--api-documentation)
22. [Code Quality & SOLID Principles](#22-code-quality--solid-principles)
23. [Phase 20 — Production Failure Scenarios](#23-phase-20--production-failure-scenarios)
24. [Key Source Files Reference](#24-key-source-files-reference)
25. [Maven Dependencies & Versions](#25-maven-dependencies--versions)
26. [Interview Question Bank](#26-interview-question-bank)
27. [How to Run](#27-how-to-run)
28. [Recommended Next Steps](#28-recommended-next-steps)

---

## 0. Business Requirements

### Layman's explanation

This platform is a **personal job-hunting CRM**. Every day you manually log job opportunities you discover — title, company, skills, salary, location, and so on. You can search and filter your pipeline, export filtered results to Excel, and view analytics on a dashboard to understand where your opportunities are concentrated.

### What each job record supports

| Field | Description |
|-------|-------------|
| Job title | Position title (e.g. "Senior Java Developer") |
| Company | Employer name (normalized in DB) |
| Role | Role category (e.g. "Backend Engineer") |
| Description | Full job description text |
| Skills | Required skills (Java, Spring, etc.) |
| Technologies | Tech stack (Kafka, PostgreSQL, etc.) |
| Country / State / City | Geographic location |
| Location | Free-text location description |
| Work mode | REMOTE, HYBRID, or ONSITE |
| Experience range | Min/max years of experience |
| Salary | Min/max salary with currency |
| Employment type | Full-time, part-time, contract, etc. |
| Job source | LinkedIn, Indeed, referral, etc. |
| Job URL | Link to original posting |
| Posted date | When the job was posted |
| Application deadline | Last date to apply |
| Created / Updated date | System timestamps |
| Status | ACTIVE, APPLIED, INTERVIEWING, OFFERED, etc. |
| Notes | Personal notes |
| Tags | Free-form tags |

### User capabilities

| Capability | Implementation |
|------------|----------------|
| Add jobs | `POST /api/jobs` |
| Edit jobs | `PUT /api/jobs/{id}` with optimistic locking |
| Delete jobs | `DELETE /api/jobs/{id}` |
| View jobs | `GET /api/jobs/{id}` |
| Search jobs | `search` query param (title, role, description, company) |
| Filter jobs | Company, country, city, role, skill, technology, work mode, experience, source, status, date range |
| Sort jobs | Spring `Pageable` sort param (e.g. `sort=createdAt,desc`) |
| Paginate jobs | `page` and `size` params |
| Filter by date/time range | `postedFrom`, `postedTo`, `createdFrom`, `createdTo` |
| Download filtered jobs as Excel | `POST /api/export` (async) |
| View dashboard analytics | `GET /api/dashboard` |

### Why this matters for interviews

Interviewers often ask: *"Walk me through a system you built."* This platform demonstrates you can take a real business problem (job tracking), model it in a normalized database, expose it via REST, and support analytics and export — all with production-grade patterns (outbox, CQRS, JWT, observability).

---

## 1. Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Language | Java 21 | Records, virtual threads-ready, LTS alignment |
| Framework | Spring Boot 3.3.x | Auto-config, actuator, native Jakarta EE |
| Microservices | Spring Cloud Gateway 2023.0.x | Reactive API gateway, routing, filters |
| Persistence | Spring Data JPA / Hibernate | ORM, Specifications, optimistic locking |
| Database | PostgreSQL 16 | ACID, JSONB, partial indexes |
| Migrations | Flyway | Versioned, reviewable schema evolution |
| Messaging | Apache Kafka + Spring Kafka | Async event backbone, at-least-once delivery |
| Security | Spring Security + JWT (jjwt) | Stateless auth, RBAC foundation |
| Frontend | React 18 + TypeScript + Vite | SPA dashboard, filters, charts |
| Export | Apache POI 5.x | Excel (.xlsx) generation |
| Resilience | Resilience4j | Circuit breaker, retry, rate limiter |
| Metrics | Micrometer + Prometheus | HTTP latency, JVM, pool, consumer lag |
| Tracing | OpenTelemetry + Jaeger | Distributed trace propagation |
| Logging | SLF4J + Logback | Structured logs with correlationId/traceId |
| Testing | JUnit 5, Mockito, Testcontainers | Unit, integration, Kafka/Postgres tests |
| Containerization | Docker + Docker Compose | Single-command local stack |

**Build:** Maven multi-module (`pom.xml` parent manages dependency versions).

---

## 2. Project Structure

```
job-intelligence-platform/
├── pom.xml                          # Parent: Java 21, Spring Boot 3.3.5, Spring Cloud 2023.0.3
├── docker-compose.yml               # Postgres, Kafka, Prometheus, Grafana, Jaeger, all services
├── README.md                        # Quick start
├── docs/
│   ├── TECH_DETAILS.md              # This file
│   ├── ARCHITECTURE.md              # Service boundaries, Kafka topics, CQRS
│   ├── DATABASE.md                  # Schema, indexes, Flyway strategy
│   └── FAILURE_SCENARIOS.md         # 25 production failure walkthroughs
├── common/
│   ├── common-core/                 # Exceptions, GlobalExceptionHandler, CorrelationIdFilter
│   ├── common-events/               # DomainEvent, KafkaTopics, EventTypes
│   └── common-security/             # JwtTokenProvider
├── services/
│   ├── api-gateway/                 # Spring Cloud Gateway, JWT, correlation ID
│   ├── auth-service/                # Register/login, JWT, RBAC foundation
│   ├── job-service/                 # CRUD, JPA Specs, Outbox, Flyway V1–V4
│   ├── dashboard-service/           # CQRS read model, Kafka consumer
│   ├── export-service/              # Async Excel via Kafka + Apache POI
│   ├── audit-service/               # Immutable audit trail from Kafka
│   └── notification-service/        # Export completion notifications
├── frontend/                        # React + TypeScript + Vite
└── infrastructure/
    ├── postgres/init-databases.sql  # Per-service DB bootstrap
    ├── prometheus/prometheus.yml    # Scrape targets
    └── docker/Dockerfile.service    # Multi-stage service image
```

---

## 3. Phase 1 — Project Architecture

### Layman's explanation

Instead of one big application with one database, the platform is split into **specialized services** — like departments in a company. Each department owns its own filing cabinet (database) and communicates via **Kafka** (async mail) for events and **HTTP** (direct calls) when an immediate answer is needed. Shared code lives in `common/*` so event schemas and error formats stay consistent across services.

### Service map

| Service | Port | Owns DB | Key responsibility |
|---------|------|---------|-------------------|
| **API Gateway** | 8080 | — | Route, JWT validation, correlation/trace ID propagation |
| **Auth Service** | 8081 | `auth_db` | Users, login, JWT access/refresh, BCrypt password hashing |
| **Job Service** | 8082 | `job_db` | Write model: CRUD, dynamic filters, transactional outbox |
| **Dashboard Service** | 8083 | `dashboard_db` | Read model: analytics aggregates (CQRS) |
| **Export Service** | 8084 | `export_db` | Async Excel generation, export history |
| **Audit Service** | 8085 | `audit_db` | Immutable audit trail from job events |
| **Notification Service** | 8086 | — | Export completion/failure alerts (extensible) |

### Why 7 services (not more)?

Each service has a **distinct scaling profile** and **database ownership**:

- Job API: high read/write, normalized schema
- Dashboard: read-heavy, denormalized aggregates
- Export: CPU-heavy, long-running, must not block HTTP
- Audit: append-only, compliance-oriented

Creating services merely for the sake of count adds operational cost (deployments, monitoring, network hops) without benefit.

### Architectural decisions

| Decision | Problem it solves | If we skip it | Alternatives | Trade-off |
|----------|-------------------|---------------|--------------|-----------|
| Maven multi-module | Shared libs, single version BOM | Copy-paste common code | Gradle composite builds | Maven XML verbosity |
| `common-events` module | Consistent event schema | Schema drift, broken consumers | Avro/Protobuf schema registry | Extra module to maintain |
| Database per service | Independent migrations & scaling | Coupling, shared schema changes | Schema-per-service in one PG instance | More DB instances to manage |
| API Gateway | Single entry, centralized auth | Auth duplicated in every service | BFF per client, service mesh | Single point of failure (mitigate with replicas) |

### Kafka topics

| Topic | Producer | Consumer(s) | Partition key |
|-------|----------|-------------|---------------|
| `job.created` | Job Service (Outbox) | Dashboard, Audit | `jobId` |
| `job.updated` | Job Service (Outbox) | Dashboard, Audit | `jobId` |
| `job.deleted` | Job Service (Outbox) | Dashboard, Audit | `jobId` |
| `export.requested` | Export Service | Export Worker | `exportId` |
| `export.completed` | Export Service | Notification | `exportId` |
| `export.failed` | Export Service | Notification | `exportId` |
| `audit.events` | All services (future) | Audit Service | `aggregateId` |
| `notification.events` | Export, Job (optional) | Notification Service | `userId` |

### Synchronous vs asynchronous communication

**Synchronous (HTTP via Gateway):**
- React → Gateway → Job / Auth / Dashboard / Export / Audit
- Export Service → Job Service (fetch filtered jobs for Excel)

**Asynchronous (Kafka):**
- Job write → Outbox → Kafka → Dashboard materialized views
- Export request → Kafka → background Excel worker
- Failures → DLQ (production) → manual replay

### Interview questions — Phase 1

1. **Why microservices for a job tracker?** Independent scaling (export workers vs job API), team ownership, failure isolation. Trade-off: operational complexity.
2. **Why not a monolith?** Valid for MVP; microservices pay off when read/write patterns diverge (CQRS) or export blocks the API.
3. **What production issue does service separation prevent?** A slow Excel export taking down job CRUD; a dashboard query saturating the write DB.

### Important code files (Phase 1)

| File | Purpose |
|------|---------|
| `pom.xml` (root) | Parent BOM: Java 21, Spring Boot 3.3.5, Spring Cloud 2023.0.3 |
| `common/common-events/.../DomainEvent.java` | Standard event envelope for all Kafka messages |
| `common/common-events/.../KafkaTopics.java` | Topic name constants |
| `docs/ARCHITECTURE.md` | Service boundary reference |

### Production considerations

- Each service deploys independently — use health checks before routing traffic
- Kafka topics must exist before consumers start (auto-create in dev; explicit in prod)
- JWT secret must be identical across Gateway and Auth Service
- Never share database credentials between services

### How to test Phase 1

- Verify all modules compile: `mvn clean package -DskipTests`
- Smoke test: start Postgres + Kafka + one service, hit `/actuator/health`

### How to debug Phase 1

- Service won't start → check Flyway migration errors in logs
- Gateway 502 → verify downstream service URL env vars in `docker-compose.yml`

### Interview deep-dive (10 questions) — Microservices

| # | Question | Answer |
|---|----------|--------|
| 1 | Why did we choose microservices? | Different scaling profiles: export is CPU-heavy, dashboard is read-heavy |
| 2 | What problem does it solve? | Independent deploy, failure isolation, team ownership |
| 3 | What if we don't use it? | Monolith works for MVP; export blocks API threads at scale |
| 4 | Alternatives? | Modular monolith, serverless functions per export |
| 5 | Disadvantages? | Network latency, distributed debugging, more infra |
| 6 | Interviewer asks? | "How do services stay consistent?" → Outbox + idempotent consumers |
| 7 | Production issue prevented? | Export OOM killing job CRUD |
| 8 | How to debug? | Correlation ID across gateway → service → Kafka → consumer |
| 9 | How to scale? | Horizontal replicas per service; Kafka partitions by jobId |
| 10 | Trade-off? | Complexity vs flexibility |

## 4. Phase 2 — Database Design

### Layman's explanation

The database is organized like a well-labeled filing system. Jobs link to companies, locations, skills, and technologies through separate tables so we don't repeat "Google" or "Java" hundreds of times. Flyway migrations are **numbered recipe cards** — each schema change is versioned and applied in order, never guessed by Hibernate at startup.

### Core tables (`job_db`)

| Table | Purpose |
|-------|---------|
| `jobs` | Core job record with FK to company, location, source |
| `companies` | Normalized company names (unique constraint) |
| `locations` | Country / state / city (indexed for geo filters) |
| `skills` / `technologies` | Tag dictionaries |
| `job_skills` / `job_technologies` | Many-to-many join tables |
| `job_tags` | Free-form tags |
| `job_sources` | LinkedIn, Indeed, referral, etc. |
| `outbox_events` | Transactional outbox for Kafka publishing |

### Important indexes and why they exist

| Index | Query pattern it serves |
|-------|--------------------------|
| `idx_jobs_user_status_created` | List user's jobs sorted by date — most common query |
| `idx_jobs_company_id` | Filter by company |
| `idx_locations_country_city` | Filter by country + city together |
| `idx_job_skills_skill_id` | Reverse lookup: all jobs requiring a skill |
| `idx_outbox_pending` | Partial index — only unpublished outbox events |
| `uq_jobs_user_url` | Prevent duplicate job URLs per user |

### Flyway migrations (Job Service)

| Version | Change | Strategy |
|---------|--------|----------|
| V1 | Initial normalized schema | Baseline |
| V2 | `work_mode_notes` column | Expand: nullable column, backward compatible |
| V3 | `salary_min`, `salary_max`, `salary_currency` | Expand: nullable first; NOT NULL in later phase after backfill |
| V4 | Source enrichment + duplicate URL index | Expand: additive columns and index |

**Never use `spring.jpa.hibernate.ddl-auto=update` in production.**

### Migration patterns explained

| Pattern | Steps | Zero-downtime? |
|---------|-------|----------------|
| **Expand-and-contract** | Add nullable column → backfill → add NOT NULL → remove old | Yes |
| **Adding nullable column** | `ALTER TABLE ADD COLUMN` | Yes — old app ignores it |
| **Adding NOT NULL safely** | Add nullable → backfill default → add constraint | Yes |
| **Adding indexes safely** | `CREATE INDEX CONCURRENTLY` in prod | Yes |
| **Column rename** | Add new → dual-write → backfill → switch reads → drop old | Yes |
| **Rollback** | Forward-fix migration (Flyway undo in Teams) | Plan ahead |

See [DATABASE.md](DATABASE.md) for full details.

### Per-service databases

| Database | Service | Key tables |
|----------|---------|------------|
| `auth_db` | Auth | `users`, `refresh_tokens` |
| `job_db` | Job | `jobs`, `companies`, `locations`, `skills`, `technologies`, `outbox_events` |
| `dashboard_db` | Dashboard | `dashboard_summary`, `dimension_counts`, `processed_events` |
| `export_db` | Export | `export_jobs` |
| `audit_db` | Audit | `audit_records` |

### Production considerations

- Use connection pooling per service (don't share pools across services)
- Run `CREATE INDEX CONCURRENTLY` for large tables in production
- Back up before Flyway migrations; test migrations on staging copy first
- Partial indexes (`idx_outbox_pending`) reduce index size and write overhead

### How to test Phase 2

- Flyway migrations apply cleanly: start job-service against empty Postgres
- Verify constraints: duplicate company name, duplicate user+URL rejected

### How to debug Phase 2

- Migration failed → `SELECT * FROM flyway_schema_history ORDER BY installed_rank`
- Slow query → `EXPLAIN ANALYZE` + check index usage in EXPLAIN output

---

## 5. Phase 3–5 — Job Service

### Layman's explanation

The Job Service is the **write brain** of the platform. Users add, edit, delete, search, filter, sort, and paginate job opportunities. Every change is saved to PostgreSQL and, within the same database transaction, an outbox event is recorded so other services find out asynchronously.

### Job fields supported

Title, company, role, description, skills, technologies, country, state/province, city, location, remote/hybrid/onsite, experience range, salary, employment type, job source, job URL, posted date, application deadline, created/updated dates, status, notes, tags.

### Layer architecture

```
JobController          → REST endpoints, validation, OpenAPI
JobService             → @Transactional business logic, outbox publish
JobRepository          → JpaRepository + JpaSpecificationExecutor
JobSpecifications      → Dynamic filtering (no if/else chains)
JobMapper              → Entity ↔ DTO mapping
OutboxService          → Save outbox row in same transaction
OutboxPublisher        → Scheduled poller → Kafka
GlobalExceptionHandler → Consistent error responses
```

### Dynamic filtering with JPA Specifications

`JobSpecifications.withFilters()` composes predicates for any combination of:

- Company + country + city + role
- Skill + technology
- Work mode + experience range
- Date range (posted / created)
- Status + employment type + source
- Full-text search across title, role, description, company

**Why not if/else blocks?** Specifications are composable, unit-testable, and generate SQL that uses indexes. Adding a new filter is one predicate, not another branch in a 200-line method.

### REST API endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/jobs` | Create job (201) |
| GET | `/api/jobs/{id}` | Get by ID |
| GET | `/api/jobs` | Search, filter, sort, paginate |
| PUT | `/api/jobs/{id}` | Update (requires `version` for optimistic lock) |
| DELETE | `/api/jobs/{id}` | Delete (204) |

Query parameters map to `JobFilterCriteria`: `search`, `company`, `country`, `city`, `role`, `skill`, `technology`, `workMode`, `experienceMin`, `experienceMax`, `source`, `status`, `postedFrom`, `postedTo`, `createdFrom`, `createdTo`.

Pagination via Spring `Pageable` (`page`, `size`, `sort`).

### Transaction + Outbox (same transaction)

```java
@Transactional
public JobResponse createJob(UUID userId, JobCreateRequest request) {
    Job saved = jobRepository.save(job);           // 1. Persist job
    publishOutboxEvent(saved, EventTypes.JOB_CREATED); // 2. Persist outbox row
    return jobMapper.toResponse(saved);            // Both commit or both rollback
}
// OutboxPublisher (scheduled) polls PENDING rows → publishes to Kafka
```

**What happens if Kafka is down?** Job saves successfully; outbox rows stay `PENDING`. Publisher retries every 2 seconds when Kafka returns. No events are lost.

**What happens if DB fails?** Neither job nor outbox row is committed. Client gets an error and can retry.

### Optimistic locking

- Column: `jobs.version` with JPA `@Version`
- Client sends `version` on PUT; mismatch → **409 OPTIMISTIC_LOCK_CONFLICT**
- Prevents lost updates when two tabs edit the same job

### Duplicate job prevention

Unique partial index `uq_jobs_user_url` on `(user_id, job_url)` where URL is not null/empty → **409 DUPLICATE_RESOURCE** on duplicate POST.

### How to test

```bash
# Unit test (Mockito)
mvn test -pl services/job-service -Dtest=JobServiceTest

# Integration test (Testcontainers — when added)
mvn verify -pl services/job-service
```

### How to debug

1. Capture `X-Correlation-Id` from response header
2. Search job-service logs: `correlationId=<id>`
3. Check `outbox_events` for unpublished rows: `SELECT * FROM outbox_events WHERE status='PENDING'`
4. Verify Flyway: `SELECT * FROM flyway_schema_history`

### Important code files (Job Service)

| File | Package / path |
|------|----------------|
| `JobController` | `com.jobintel.job.web` — REST layer |
| `JobService` | `com.jobintel.job.service` — transactional business logic |
| `JobSpecifications` | `com.jobintel.job.repository.spec` — dynamic filters |
| `JobMapper` | `com.jobintel.job.mapper` — entity ↔ DTO |
| `OutboxPublisher` | `com.jobintel.job.outbox` — scheduled Kafka publisher |
| `V1__initial_schema.sql` | `src/main/resources/db/migration/` — baseline schema |

### DTOs and validation

| DTO | Purpose |
|-----|---------|
| `JobCreateRequest` | POST body with `@NotBlank`, `@NotEmpty`, `@NotNull` validation |
| `JobUpdateRequest` | PUT body; requires `version` for optimistic lock |
| `JobResponse` | API response including `userId`, all job fields, timestamps |
| `JobFilterCriteria` | Query params bound for dynamic filtering |

### Production considerations

- Keep `@Transactional` on service layer, not controller
- `open-in-view: false` prevents lazy-loading outside transaction
- Outbox poll interval tunable via `job.outbox.poll-interval-ms`
- Paginate all list endpoints — never return unbounded result sets
- Index-backed filters only — avoid `%leading wildcard` in production search if possible

### How to test Job Service

```bash
# Unit: duplicate URL rejection
mvn test -pl services/job-service -Dtest=JobServiceTest

# Manual API test (via gateway, after login)
curl -H "Authorization: Bearer $TOKEN" -H "X-User-Id: $USER_ID" \
  "http://localhost:8080/api/jobs?company=Google&workMode=REMOTE&page=0&size=20"
```

### Interview deep-dive (10 questions) — Outbox Pattern

| # | Question | Answer |
|---|----------|--------|
| 1 | Why Outbox? | Atomic DB write + event record; no lost events on Kafka failure |
| 2 | Problem solved? | Dual-write inconsistency (DB committed, Kafka message lost) |
| 3 | Without it? | Dashboard permanently out of sync after Kafka outage |
| 4 | Alternatives? | Debezium CDC, XA/2PC (avoid), transactional Kafka (complex) |
| 5 | Disadvantages? | Eventual delivery latency (poll interval), extra table |
| 6 | Interviewer asks? | "What if publisher crashes mid-batch?" → Unpublished rows retried |
| 7 | Production issue? | Silent data loss between services |
| 8 | Debug? | `SELECT * FROM outbox_events WHERE status='PENDING'` |
| 9 | Scale? | Multiple publisher instances with `SELECT FOR UPDATE SKIP LOCKED` |
| 10 | Trade-off? | Latency (seconds) vs consistency guarantee |

---

## 6. Phase 6 — React Frontend

### Pages

| Route | Component | Features |
|-------|-----------|----------|
| `/login` | LoginPage | JWT auth, error handling |
| `/dashboard` | DashboardPage | KPI cards, Recharts bar chart |
| `/jobs` | JobsPage | Filterable table, pagination, export button |
| `/jobs/new` | JobFormPage | Create job form |
| `/jobs/:id` | JobFormPage | Edit job form |
| `/exports` | ExportsPage | Export history and status |
| `/profile` | ProfilePage | User info from JWT payload |

### Tech choices

- **React 18 + TypeScript** — type-safe components
- **Vite** — fast dev server with API proxy to gateway
- **TanStack Query** — server state caching for dashboard/jobs
- **Axios** — HTTP client with JWT + correlation ID interceptors
- **Recharts** — dashboard charts

### UI features

- Responsive layout (sidebar + content)
- Filters: company, country, city, role, search
- Export button triggers async export via Export Service
- All API calls include `Authorization: Bearer <token>` and `X-Correlation-Id`

### Filters supported in UI

Company, Country, City, Role, Skill, Technology, Work Mode, Experience, Source, Date Range (via API; partial UI wiring).

### Production considerations

- Store JWT in localStorage (dev); use httpOnly cookies in production
- Handle 401 globally — redirect to login on token expiry
- React Query cache invalidation after job create/update/delete

### How to test frontend

```bash
cd frontend && npm install && npm run dev
# Open http://localhost:5173 — login, create job, check dashboard
```

---

## 7. Phase 7 — Dashboard Service (CQRS)

### Layman's explanation

The dashboard shows analytics (totals, breakdowns by company/country/skill/etc.). Instead of querying the Job Service database on every page load — which would be slow and couple services — the Dashboard Service maintains its own **read-optimized copy** updated by Kafka events.

### CQRS in this project

| Side | Component | Database | Update mechanism |
|------|-----------|----------|-----------------|
| **Write** | Job Service | `job_db` (normalized) | Direct user CRUD |
| **Read** | Dashboard Service | `dashboard_db` (denormalized) | Kafka event consumption |

### Dashboard metrics

- Total jobs, jobs today, this week, this month
- Breakdowns: company, country, city, role, technology, skill, work mode, source, experience

### Idempotent consumer

`processed_events` table stores `event_id`. Duplicate Kafka messages (at-least-once delivery) are skipped.

**Acceptable lag:** Dashboard may be seconds behind Job Service. Fine for analytics; not for financial transactions.

### Interview answer

"CQRS here means we separated the write model (normalized, transactional) from the read model (denormalized, eventually consistent). We don't claim every app needs two databases — we use it because analytics queries have different access patterns than CRUD."

### Important code files

| File | Purpose |
|------|---------|
| `JobEventConsumer` | Kafka listener for job.* topics |
| `DashboardProjectionService` | Updates summary + dimension counts; idempotent by eventId |
| `DashboardController` | `GET /api/dashboard` — read-only, never calls Job Service |

### Production considerations

- Dashboard lag of 1–5 seconds is acceptable for analytics
- Optional nightly reconciliation job can compare Job DB count vs Dashboard count
- `processed_events` table grows — archive old event IDs periodically

---

## 8. Phase 8 — Export Service

### Layman's explanation

Exporting thousands of jobs to Excel is slow. Instead of making the user stare at a spinner for 30 seconds, the Export Service accepts the request immediately, queues it on Kafka, and generates the file in the background.

### Flow

```
React → POST /api/export (202 Accepted)
     → Export Service saves export_jobs row (PENDING)
     → Publishes export.requested to Kafka
     → ExportWorker consumes, fetches jobs, generates .xlsx (Apache POI)
     → Updates status to COMPLETED (or FAILED)
     → Publishes export.completed / export.failed
     → User checks /exports page for download path
```

### Features

- Filtered export (passes filter criteria)
- Async processing (HTTP not blocked)
- Export status tracking and history
- Failure handling with error message stored

### Large export flow (do not block HTTP)

```
React → Export Service (202 Accepted, exportId returned)
      → Kafka export.requested
      → ExportWorker (separate thread pool / consumer group)
      → Fetch jobs from Job Service (paginated in production)
      → Apache POI writes .xlsx to disk / object storage
      → Status COMPLETED + download URL
      → notification.events → user notified
```

### Production considerations

- For very large exports: stream rows to Excel, don't load all jobs in memory
- Object storage (S3/MinIO) instead of local disk in production
- Export timeout and max row limit to prevent runaway jobs

---

## 9. Phase 9 — API Gateway

### Responsibilities

- **Routing:** `/api/auth/**`, `/api/jobs/**`, `/api/dashboard/**`, `/api/export/**`, `/api/audit/**`
- **JWT validation:** Global filter extracts token, validates, injects `X-User-Id` header
- **Correlation ID:** Generated or propagated via `X-Correlation-Id`
- **Rate limiting:** Configurable via Redis (Resilience4j / Spring Cloud Gateway rate limiter)
- **Circuit breaker:** Resilience4j on downstream service calls

### Public paths (no JWT)

- `/api/auth/login`
- `/api/auth/register`
- `/actuator/**`

### Gateway filters (order)

1. `CorrelationIdGatewayFilter` (order -200)
2. `JwtAuthenticationFilter` (order -100)

### Example routes

```
/api/auth/**      → auth-service:8081
/api/users/**     → auth-service:8081
/api/jobs/**      → job-service:8082
/api/dashboard/** → dashboard-service:8083
/api/export/**    → export-service:8084
/api/audit/**     → audit-service:8085
```

### Production considerations

- Run multiple gateway replicas behind load balancer
- Centralized error handling returns consistent JSON errors
- Request logging filter logs: method, URI, status, duration, correlationId

---

## 10. Phase 10 — Authentication & Authorization

### Implementation

| Feature | Detail |
|---------|--------|
| Password hashing | BCrypt (strength 12) |
| Access token | JWT, short-lived (default 15 min) |
| Refresh token | JWT, long-lived (default 7 days) |
| Roles | ADMIN, USER, VIEWER (foundation in place) |
| Token claims | `sub` (userId), `email`, `roles`, `type` |

### Auth endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Get access + refresh tokens |

### RBAC (endpoint-level authorization)

Roles stored in `users.role`. Gateway passes `X-User-Roles` header. Services can enforce with `@PreAuthorize("hasRole('ADMIN')")` — wire-up recommended as next step.

### Role permissions (design)

| Role | Permissions |
|------|-------------|
| ADMIN | Full CRUD, user management, audit access |
| USER | CRUD own jobs, export, dashboard |
| VIEWER | Read-only jobs and dashboard |

### Production considerations

- JWT secret minimum 256 bits; rotate via secrets manager
- Refresh token stored hashed in `refresh_tokens` table
- Never log tokens or passwords

---

## 11. Phase 11–12 — Kafka & Outbox Pattern

### Event envelope (`DomainEvent`)

Every event contains:

```
eventId, eventType, timestamp, source, correlationId, traceId, aggregateId, payload
```

### Producer (Job Service)

- Idempotent producer enabled (`enable.idempotence: true`)
- Partition key = `aggregateId` (job ID) for ordering per job

### Consumer patterns

| Pattern | Where | How |
|---------|-------|-----|
| Idempotent consumer | Dashboard, Audit | Dedup by `eventId` |
| Consumer groups | Each service | Independent offset tracking |
| Retry | Spring Kafka default | Exponential backoff |
| DLQ | Production config | Poison messages after N retries |
| Duplicate handling | All consumers | `processed_events` / `audit_records.event_id` unique |

### Outbox Pattern — why it exists

**Problem:** Dual-write — save to DB, then publish to Kafka. If Kafka fails after DB commit, the event is lost forever.

**Solution:** Save outbox row in the **same transaction** as the business entity. A separate poller publishes to Kafka and marks rows as PUBLISHED.

**Without outbox:** Silent data loss between services; dashboard permanently out of sync.

**Alternative:** Change Data Capture (Debezium) — reads DB WAL directly. More infra, but no application code for publishing.

### Partition strategy & ordering

- Partition key = `jobId` (aggregateId) → all events for one job go to same partition → ordering guaranteed per job
- Ordering **not** guaranteed across different jobs (different partitions)
- Consumer group: each service (dashboard, audit) has independent offset

### Offset management

- Auto-commit disabled in production recommended; commit after successful processing
- On consumer crash: uncommitted offset replayed → duplicate → idempotent handler required

---

## 12. Phase 13–14 — Dashboard Events & Audit

### Dashboard event processing

- Consumes: `job.created`, `job.updated`, `job.deleted`
- Updates: `dashboard_summary` counters, `dimension_counts` breakdowns
- Dedup: `processed_events.event_id`

### Audit Service

- Consumes same job topics
- Writes immutable `audit_records` (event_id, event_type, aggregate_id, payload JSONB, correlation_id)
- Dedup by `event_id` unique constraint
- Never updated or deleted — compliance trail

---

## 13. Phase 15 — Notification Service

### Layman's explanation

When an export finishes (or fails), the user should know without polling the exports page every 5 seconds. The Notification Service listens for export events and can send email, push, or in-app notifications.

### Implementation

- Consumes: `export.completed`, `export.failed`
- Consumer group: `notification-service`
- Currently logs events; extensible to email (SendGrid), Slack, etc.

### Production considerations

- Notifications must be idempotent (same export.completed twice → one email)
- Dead letter queue for failed notification delivery
- Template-based messages with export ID and download link

---

## 14. Phase 16 — Observability

### Structured logging

Every log line includes:

```
service, traceId, correlationId, userId, HTTP method, URI, status, duration
```

Log pattern configured in each service's `application.yml`. Search by `correlationId` across all services to trace a single user request.

### Metrics (Micrometer → Prometheus)

| Metric | Source |
|--------|--------|
| HTTP request count / latency | Spring Boot Actuator |
| Error count (4xx, 5xx) | Actuator |
| JVM memory, GC, threads | Actuator |
| HikariCP pool active/idle/pending | `hikaricp.connections.*` |
| Kafka consumer lag | Burrow / Kafka exporter (production) |
| Job creation rate | Custom counter (extensible) |
| Export duration / failure rate | Custom timer (extensible) |

### Distributed tracing (OpenTelemetry → Jaeger)

Trace path:
```
React → API Gateway → Job Service → PostgreSQL
Job Service → Kafka → Dashboard Service
```

Debug flow: get `traceId` from logs → open Jaeger UI → inspect span timeline.

### Grafana

- Pre-provisioned at port 3001 (admin/admin)
- Connect Prometheus data source
- Build dashboards for HTTP latency, error rate, pool exhaustion

---

## 15. Phase 17 — Resilience

### Patterns implemented

| Pattern | Tool | Where |
|---------|------|-------|
| Timeout | Spring / Gateway | Downstream HTTP calls |
| Retry | Spring Kafka | Consumer error handler |
| Exponential backoff | Kafka consumer config | Poison message mitigation |
| Circuit breaker | Resilience4j | Gateway → Job Service |
| Rate limiter | Gateway (Redis-backed) | Per-user API throttling |
| Bulkhead | Thread pool isolation | Export worker (separate consumer group) |
| Fallback | Circuit breaker fallback | Gateway returns 503 when circuit open |

### Important rule

**Never blindly retry non-idempotent operations.** POST /jobs without an idempotency key should not be auto-retried by the client. Outbox publisher retries are safe because the consumer deduplicates by `eventId`.

---

## 16. Phase 18 — Testing

### Test pyramid

| Level | Tool | Example |
|-------|------|---------|
| Unit | JUnit 5 + Mockito | `JobServiceTest` — duplicate URL rejection |
| Integration | Spring Boot Test + Testcontainers | Job API + PostgreSQL |
| Kafka | Testcontainers Kafka | Outbox publish → consumer receive |
| API | MockMvc / WebTestClient | Controller validation, error responses |
| Contract | WireMock | Export Service → Job Service HTTP contract |

### Meaningful test cases (not getter/setter tests)

- Duplicate job URL → 409
- Optimistic lock conflict → 409
- Filter combinations return correct subset
- Outbox row created in same transaction as job
- Dashboard consumer skips duplicate eventId
- Export worker handles Job Service timeout

### Run tests

```bash
mvn test                              # All unit tests
mvn verify -pl services/job-service   # Including integration (when added)
```

---

## 17. Phase 19–20 — Docker & Production Configuration

### Docker Compose services

Postgres, Kafka, Kafka UI, Prometheus, Grafana, Jaeger, all 7 microservices, React frontend.

### Single command start

```bash
docker compose up --build
```

### Configuration strategy

| File | Purpose |
|------|---------|
| `application.yml` | Defaults with `${ENV_VAR:default}` placeholders |
| `application-local.yml` | Local overrides (future) |
| `application-dev.yml` | Dev cluster settings (future) |
| `application-prod.yml` | Production settings (future) |

### Never hardcode

- Passwords, JWT secrets, DB credentials, Kafka credentials — all via environment variables in `docker-compose.yml` or secrets manager in production.

### Environment-specific configuration

| Profile | File | Use case |
|---------|------|----------|
| local | `application-local.yml` | Developer laptop, embedded defaults |
| dev | `application-dev.yml` | Shared dev cluster |
| prod | `application-prod.yml` | Production: external secrets, tuned pools |

Activate via: `SPRING_PROFILES_ACTIVE=prod`

### Secrets — never hardcode

| Secret | Env variable |
|--------|--------------|
| JWT secret | `JWT_SECRET` |
| Job DB | `JOB_DB_URL`, `JOB_DB_USER`, `JOB_DB_PASSWORD` |
| Auth DB | `AUTH_DB_URL`, `AUTH_DB_USER`, `AUTH_DB_PASSWORD` |
| Kafka | `KAFKA_BOOTSTRAP` |

---

## 18. Transaction Management

### Layman's explanation

A transaction is an all-or-nothing bundle. Either the job and its outbox event both save, or neither does. `@Transactional` draws the boundary around that bundle.

### Key concepts

| Concept | In this project |
|---------|----------------|
| **ACID** | PostgreSQL guarantees; outbox + job in one transaction |
| **Isolation** | Default READ COMMITTED; optimistic lock for concurrent updates |
| **Rollback** | Unchecked exceptions roll back; outbox not saved if job save fails |
| **Propagation** | `REQUIRED` (default) — join existing transaction in `JobService` |
| **Optimistic locking** | `@Version` on Job entity |
| **Pessimistic locking** | Not used (optimistic sufficient for user-edited jobs) |
| **Race conditions** | Handled by version check + unique URL index |
| **Timeout** | `@Transactional(timeout = 30)` recommended for long operations |

### What `@Transactional` does NOT do

It does **not** provide distributed transactions across microservices. Each service has its own local transaction. Cross-service consistency is achieved via **Outbox + Kafka + idempotent consumers**, not 2PC/XA.

---

## 19. Connection Pooling (HikariCP)

### Layman's explanation

Opening a database connection is expensive (network handshake, auth). HikariCP keeps a **pool of ready connections** so your app borrows one, uses it, and returns it — instead of opening a new one every request.

### Configuration (Job Service)

```yaml
spring:
  datasource:
    hikari:
      pool-name: JobServicePool
      maximum-pool-size: 10      # Max concurrent DB connections
      minimum-idle: 2            # Always-ready idle connections
      connection-timeout: 30000  # 30s max wait for a free connection
      idle-timeout: 600000       # 10min — retire unused idle connections
      max-lifetime: 1800000      # 30min — recycle connections (DB/firewall limits)
```

### What goes wrong in production

| Problem | Symptom | Fix |
|---------|---------|-----|
| **Connection leak** | Pool exhausted, all connections "active" forever | Always close resources; avoid `@Transactional` on long I/O |
| **Pool exhaustion** | Threads block 30s then timeout | Reduce transaction scope; fix slow queries; increase pool temporarily |
| **Slow queries** | Few connections held for seconds | Add indexes; paginate; move analytics to read DB |
| **Transaction holding connection** | Connection busy during external HTTP call | Don't call Kafka/HTTP inside `@Transactional` unless necessary |

### Production-safe sizing

```
pool size ≈ (CPU cores on DB * 2) / number of app instances
```

For 2 job-service replicas on a 4-core Postgres: `maximum-pool-size: 10` each is reasonable. Monitor `hikaricp.connections.active` and `hikaricp.connections.pending`.

### Interview answer

"HikariCP is the default pool in Spring Boot 2+ because it's fast and reliable. I tune `maximum-pool-size` based on workload, never set it to 100 by default, and I watch pending connections in Prometheus to catch leaks early."

---

## 20. Exception Handling

### Custom exceptions (`common-core`)

| Exception | HTTP Status | Error code |
|-----------|-------------|------------|
| `ResourceNotFoundException` | 404 | `RESOURCE_NOT_FOUND` |
| `ValidationException` | 400 | `VALIDATION_ERROR` |
| `DuplicateResourceException` | 409 | `DUPLICATE_RESOURCE` |
| `BusinessException` | 422 | Custom code |
| `ExternalServiceException` | 502 | `EXTERNAL_SERVICE_ERROR` |
| Optimistic lock | 409 | `OPTIMISTIC_LOCK_CONFLICT` |

### Error response shape

```json
{
  "timestamp": "2026-08-11T08:00:00Z",
  "status": 409,
  "errorCode": "DUPLICATE_RESOURCE",
  "message": "Job with this URL already exists for user",
  "path": "/api/jobs",
  "correlationId": "abc-123",
  "validationErrors": null
}
```

Stack traces and internal details are **never** exposed to clients. `@RestControllerAdvice` in `GlobalExceptionHandler` centralizes all responses.

---

## 21. OpenAPI & API Documentation

### Layman's explanation

OpenAPI (Swagger) generates **interactive API docs** from your code annotations — so frontend devs and interviewers can see every endpoint, request shape, validation rules, and error codes without reading source.

### Where to access

| Service | Swagger UI URL |
|---------|----------------|
| Job Service | http://localhost:8082/swagger-ui.html |
| Auth Service | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | http://localhost:8082/api-docs |

### What every API documents

- Request body schema with validation (`@NotBlank`, `@Size`, etc.)
- Response schema (`JobResponse`, `ErrorResponse`)
- Possible HTTP status codes (201, 400, 404, 409, 422, 500)
- Authentication requirement (Bearer JWT)
- Example values via `@Schema` annotations (extensible)

### Production considerations

- Disable Swagger UI in production or protect behind auth
- OpenAPI spec can drive contract tests and client code generation

---

## 22. Code Quality & SOLID Principles

### Principles applied in this project

| Principle | Example in codebase |
|-----------|---------------------|
| **Single Responsibility** | `JobController` only handles HTTP; `JobService` only business logic |
| **Open/Closed** | New filter = new predicate in `JobSpecifications`, not editing existing method |
| **Liskov Substitution** | Repository interfaces swappable for tests |
| **Interface Segregation** | Separate repos per aggregate (Company, Skill, Job) |
| **Dependency Inversion** | Services depend on repository interfaces, not JDBC |

### Code quality rules followed

- No `System.out.println` — SLF4J + Logback only
- Immutable DTOs via Java `record` types
- Small methods; no god classes
- No duplicated business logic across services
- Meaningful naming (`JobFilterCriteria`, not `FilterDTO`)
- No unnecessary abstractions (no extra wrapper layers without purpose)

### What we deliberately avoid

- Over-engineering: no generic `AbstractBaseService` for one entity
- Premature optimization: CQRS only where read pattern differs
- Distributed transactions (2PC): outbox instead

---

## 23. Phase 20 — Production Failure Scenarios

For each scenario: **what happens**, **why**, **detection**, **recovery**, **debugging**.

See also [FAILURE_SCENARIOS.md](FAILURE_SCENARIOS.md) for the standalone reference.

### 1. Duplicate job creation

**Layman:** User double-clicks Save or retries after timeout.

| Aspect | Detail |
|--------|--------|
| What happens | Unique index `uq_jobs_user_url` rejects duplicate → HTTP 409 |
| Why | No idempotency key on POST; network retry resubmits same payload |
| Detection | HTTP 409, metric `http.server.requests{status=409}` |
| Recovery | Show "already exists"; future: `Idempotency-Key` header |
| Debug | Search logs by `correlationId`; check `jobs` table |

### 2. Kafka message duplication

**Layman:** Kafka delivers at-least-once; same message may arrive twice.

| Aspect | Detail |
|--------|--------|
| What happens | Dashboard/Audit skip duplicate `eventId` |
| Detection | Debug log "Skipping duplicate event" |
| Recovery | Automatic (idempotent consumer) |
| Debug | Compare Kafka `eventId` vs `processed_events` table |

### 3. Kafka unavailable

| Aspect | Detail |
|--------|--------|
| What happens | Jobs save; outbox stays PENDING; publisher retries every 2s |
| Detection | Growing pending outbox count; Kafka broker alerts |
| Recovery | Restore Kafka; outbox drains automatically |
| Debug | `SELECT count(*) FROM outbox_events WHERE status='PENDING'` |

### 4. Database unavailable

| Aspect | Detail |
|--------|--------|
| What happens | HikariCP timeout → 500/503; circuit breaker may open |
| Detection | Actuator health DOWN; `hikaricp.connections.timeout` |
| Recovery | Restore Postgres; restart pods |
| Debug | Active connections vs max pool size |

### 5. Connection pool exhausted

| Aspect | Detail |
|--------|--------|
| What happens | Threads block on `getConnection()` until timeout |
| Detection | `hikaricp.connections.pending` > 0 sustained |
| Recovery | Fix slow queries; shorten transactions; scale replicas |
| Debug | Thread dump; find long `@Transactional` methods |

### 6. Slow database query

| Aspect | Detail |
|--------|--------|
| What happens | Request exceeds gateway timeout |
| Detection | Trace span duration; `pg_stat_statements` |
| Recovery | Add index; paginate; use dashboard read model |
| Debug | `EXPLAIN ANALYZE` on Hibernate SQL |

### 7. Dashboard behind Job Service

| Aspect | Detail |
|--------|--------|
| What happens | CQRS lag — analytics a few seconds stale |
| Detection | Consumer lag metric; count mismatch |
| Recovery | Self-heals; optional reconciliation job |
| Debug | Kafka UI → `dashboard-service` consumer group lag |

### 8–10. Export / Excel failures

| Aspect | Detail |
|--------|--------|
| What happens | Worker exception → status FAILED → `export.failed` event |
| Detection | Export history UI; export UUID in logs |
| Recovery | User retries; ops replays Kafka message |
| Debug | `export_jobs.error_message` for failed export ID |

### 11. Poison Kafka message

| Aspect | Detail |
|--------|--------|
| What happens | Deserialization fails N times → DLQ (production config) |
| Detection | DLQ topic depth alert |
| Recovery | Fix schema; replay or skip offset |
| Debug | Raw message in DLQ vs `DomainEvent` schema |

### 12–13. Schema migration / backward compatibility

| Aspect | Detail |
|--------|--------|
| What happens | Flyway V3 adds nullable `salary_min`; old app ignores it |
| Detection | `flyway_schema_history`; deploy pipeline gate |
| Recovery | Expand-and-contract; never drop in same release |
| Debug | Migration version vs app version in logs |

### 14–15. Concurrent updates / optimistic lock

| Aspect | Detail |
|--------|--------|
| What happens | Second tab save → 409 OPTIMISTIC_LOCK_CONFLICT |
| Detection | HTTP 409 response |
| Recovery | User refreshes; retries with new `version` |
| Debug | Compare request `version` vs DB row |

### 16–19. Timeouts, retry storms, circuit breaker

| Aspect | Detail |
|--------|--------|
| What happens | Resilience4j opens circuit → fast 503 instead of hang |
| Detection | `resilience4j_circuitbreaker_state` metric |
| Recovery | Fix root cause; circuit half-opens automatically |
| Debug | Trace shows failing hop; don't retry non-idempotent POST |

### 20–25. Restarts, partial failures, outbox crash

| Scenario | Behavior |
|----------|----------|
| Outbox publisher crash | PENDING rows republished on restart; consumers dedupe |
| Network failure after DB commit | Outbox row committed with job — event not lost |
| Consumer crash | Offset not committed → replay → idempotent skip |
| Service restart | Kafka consumer rebalances; resumes from last offset |
| Partial failure | Job saved, dashboard not updated yet — self-heals via outbox |

**Universal debug playbook:** `X-Correlation-Id` → service logs → Jaeger `traceId` → Kafka offset/lag → DB state.

---

## 24. Key Source Files Reference

### Common modules

| File | Path |
|------|------|
| GlobalExceptionHandler | `common/common-core/.../exception/GlobalExceptionHandler.java` |
| CorrelationIdFilter | `common/common-core/.../web/CorrelationIdFilter.java` |
| DomainEvent | `common/common-events/.../events/DomainEvent.java` |
| JwtTokenProvider | `common/common-security/.../security/JwtTokenProvider.java` |

### Job Service

| File | Path |
|------|------|
| JobServiceApplication | `services/job-service/.../JobServiceApplication.java` |
| JobController | `services/job-service/.../web/JobController.java` |
| JobService | `services/job-service/.../service/JobService.java` |
| JobSpecifications | `services/job-service/.../repository/spec/JobSpecifications.java` |
| OutboxPublisher | `services/job-service/.../outbox/OutboxPublisher.java` |
| Flyway V1–V4 | `services/job-service/src/main/resources/db/migration/` |
| application.yml | `services/job-service/src/main/resources/application.yml` |

### Auth & Gateway

| File | Path |
|------|------|
| AuthController | `services/auth-service/.../web/AuthController.java` |
| JwtAuthenticationFilter | `services/api-gateway/.../filter/JwtAuthenticationFilter.java` |
| Gateway routes | `services/api-gateway/src/main/resources/application.yml` |

### Dashboard, Export, Audit

| File | Path |
|------|------|
| JobEventConsumer | `services/dashboard-service/.../kafka/JobEventConsumer.java` |
| DashboardProjectionService | `services/dashboard-service/.../service/DashboardProjectionService.java` |
| ExportWorker | `services/export-service/.../kafka/ExportWorker.java` |
| AuditEventConsumer | `services/audit-service/.../kafka/AuditEventConsumer.java` |

### Frontend

| File | Path |
|------|------|
| App routes | `frontend/src/App.tsx` |
| API client | `frontend/src/api/client.ts` |
| Jobs page | `frontend/src/pages/JobsPage.tsx` |
| Dashboard | `frontend/src/pages/DashboardPage.tsx` |

### Infrastructure

| File | Path |
|------|------|
| docker-compose.yml | Project root |
| Postgres init | `infrastructure/postgres/init-databases.sql` |
| Service Dockerfile | `infrastructure/docker/Dockerfile.service` |
| Prometheus | `infrastructure/prometheus/prometheus.yml` |

---

## 25. Maven Dependencies & Versions

Managed in root `pom.xml`:

| Dependency | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.3.5 |
| Spring Cloud | 2023.0.3 |
| MapStruct | 1.6.2 |
| Lombok | 1.18.34 |
| SpringDoc OpenAPI | 2.6.0 |
| Testcontainers | 1.20.3 |
| Resilience4j | 2.2.0 |
| Apache POI | 5.3.0 |
| JJWT | 0.12.6 |
| PostgreSQL driver | Managed by Spring Boot BOM |
| Flyway | Managed by Spring Boot BOM |

Build all modules:

```bash
mvn clean package -DskipTests
```

Build single service:

```bash
mvn package -pl services/job-service -am -DskipTests
```

---

## 26. Interview Question Bank

For every major decision, prepare to answer:

1. **Why did we choose it?**
2. **What problem does it solve?**
3. **What happens if we don't use it?**
4. **What are the alternatives?**
5. **What are the disadvantages?**
6. **What would an interviewer ask?**
7. **What production issue does it prevent?**
8. **How would I debug it?**
9. **How would this scale?**
10. **What trade-off are we accepting?**

### Quick-reference answers

| Topic | One-line answer |
|-------|----------------|
| Spring Boot | Convention-over-configuration; production-ready defaults |
| JPA Specifications | Composable type-safe queries without string concatenation |
| Flyway | Version-controlled schema; no surprise DDL in production |
| Kafka | Decouples services; buffers spikes; enables replay |
| Outbox | Atomic DB + event write; prevents lost events |
| CQRS | Separate read/write models for different access patterns |
| JWT | Stateless auth; gateway validates once, services trust headers |
| HikariCP | Fast, reliable connection pool; tune pool size to workload |
| Optimistic lock | Better concurrency than pessimistic for low-conflict edits |
| Testcontainers | Real Postgres/Kafka in tests; no mocks for infra |
| Docker Compose | Reproducible local stack matching production topology |

### Topics you can confidently discuss using this project

Spring Boot, REST, JPA/Hibernate, PostgreSQL, transactions, concurrency, Kafka, microservices, Outbox Pattern, CQRS, distributed tracing, structured logging, monitoring, security, database migration, connection pooling, resilience, Docker, testing, API Gateway, scaling, and production debugging.

---

## 27. How to Run

### Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose (for full stack)

### Build

```bash
cd ~/Projects/job-intelligence-platform
mvn clean package -DskipTests
```

### Start full stack

```bash
docker compose up --build
```

### Register and login

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@jobintel.com","password":"password123","fullName":"Demo User"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@jobintel.com","password":"password123"}'
```

### Service URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| API Gateway | http://localhost:8080 |
| Job Swagger | http://localhost:8082/swagger-ui.html |
| Auth Swagger | http://localhost:8081/swagger-ui.html |
| Kafka UI | http://localhost:8090 |
| Grafana | http://localhost:3001 (admin/admin) |
| Jaeger | http://localhost:16686 |
| Prometheus | http://localhost:9090 |

### Frontend dev mode

```bash
cd frontend && npm install && npm run dev
```

Vite proxies `/api` to `http://localhost:8080`.

---

## 28. Recommended Next Steps

These items are documented for interview depth. Implement them to strengthen the portfolio further.

1. **Testcontainers integration tests** — full Job API + Postgres + Kafka end-to-end
2. **WireMock contract tests** — Export Service → Job Service HTTP boundary
3. **OpenTelemetry Java agent** — auto-instrumentation in Dockerfiles (trace propagation without manual spans)
4. **Grafana dashboard JSON** — pre-built panels for job creation rate, export duration, consumer lag
5. **Redis rate limiter** on gateway — wire Redis into docker-compose for per-user throttling
6. **RBAC enforcement** — `@PreAuthorize` on Job endpoints for ADMIN / USER / VIEWER
7. **DLQ configuration** — Kafka `DefaultErrorHandler` with dead-letter topic for poison messages
8. **JSON structured logging** — Logback JSON encoder for log aggregation (ELK/Datadog)
9. **Refresh token rotation** — store refresh tokens in `refresh_tokens` table, revoke on logout
10. **Idempotency keys** — `Idempotency-Key` header on POST /jobs for safe client retries

### Detailed rationale for each next step

| # | Item | Why it matters | Interview talking point |
|---|------|----------------|------------------------|
| 1 | Testcontainers | Proves integration works against real Postgres/Kafka | "I don't mock the database in integration tests" |
| 2 | WireMock | Validates Export→Job HTTP contract without running both services | "Contract tests catch breaking API changes" |
| 3 | OTel Java agent | Zero-code trace propagation across all hops | "I can debug latency across 4 services in Jaeger" |
| 4 | Grafana dashboards | Visual SLO tracking | "I alert when p99 latency exceeds 500ms" |
| 5 | Redis rate limiter | Per-user API throttling | "Prevents one user from exhausting the cluster" |
| 6 | RBAC `@PreAuthorize` | Endpoint-level security | "VIEWER role cannot DELETE jobs" |
| 7 | Kafka DLQ | Poison message isolation | "Bad messages don't block the whole consumer" |
| 8 | JSON logging | ELK/Datadog searchability | "I find all logs for correlationId in one query" |
| 9 | Refresh token rotation | Stolen refresh token mitigation | "Old refresh tokens revoked on reuse" |
| 10 | Idempotency keys | Safe POST retries | "Double-click Save doesn't create duplicate jobs" |

---

## Documentation Index

| Document | Contents |
|----------|----------|
| **TECH_DETAILS.md** (this file) | Complete technical reference: all phases, stack, patterns, failure scenarios, interview Q&A, source files, how to run |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Service boundaries, Kafka topics, sync/async communication |
| [DATABASE.md](DATABASE.md) | Schema, indexes, Flyway migration patterns |
| [FAILURE_SCENARIOS.md](FAILURE_SCENARIOS.md) | Standalone quick-reference for 25 production failures |
| [README.md](../README.md) | Quick start, ports, curl examples |

---

*This document is the single source of truth for technical implementation details. Update it as new phases are completed.*
