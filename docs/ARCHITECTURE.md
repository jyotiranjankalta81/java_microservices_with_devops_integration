# Job Intelligence Platform — Architecture

## Layman's Summary

Think of this platform as a **personal job-hunting CRM**. You log every opportunity you find, filter and search them, export to Excel, and see analytics on a dashboard. Under the hood it is split into small services so each part can scale, fail, and deploy independently — like departments in a company that talk via email (Kafka) instead of sharing one filing cabinet (database).

## Service Responsibilities

| Service | Responsibility | Database | Sync Calls | Async (Kafka) |
|---------|---------------|----------|------------|---------------|
| **API Gateway** | Routing, JWT validation, rate limiting, correlation/trace IDs | None | Routes to all services | None |
| **Auth Service** | Users, login, refresh tokens, RBAC | `auth_db` | None (called by Gateway) | Publishes user events (optional) |
| **Job Service** | Job CRUD, search, filter, sort, pagination | `job_db` | None | Publishes `job.*` via Outbox |
| **Dashboard Service** | Read-optimized analytics (CQRS read model) | `dashboard_db` | None | Consumes `job.*` |
| **Export Service** | Async Excel generation, export history | `export_db` | Calls Job Service (filtered query) | `export.*` topics |
| **Audit Service** | Immutable audit trail | `audit_db` | None | Consumes `audit.events` |
| **Notification Service** | Email/in-app notifications | `notification_db` | None | Consumes `notification.events` |

## Database Ownership

Each service owns its schema. **No cross-service DB access.**

- `job_db`: jobs, companies, locations, skills, technologies, job_skills, job_technologies, job_sources, outbox_events
- `auth_db`: users, roles, refresh_tokens
- `dashboard_db`: denormalized aggregates (jobs_by_company, jobs_by_country, daily_counts, etc.)
- `export_db`: export_jobs, export_files
- `audit_db`: audit_records
- `notification_db`: notifications, delivery_status

## Kafka Topics

| Topic | Producer | Consumer(s) | Partition Key |
|-------|----------|-------------|---------------|
| `job.created` | Job Service (Outbox) | Dashboard, Audit | `jobId` |
| `job.updated` | Job Service (Outbox) | Dashboard, Audit | `jobId` |
| `job.deleted` | Job Service (Outbox) | Dashboard, Audit | `jobId` |
| `export.requested` | Export Service | Export Service (worker) | `exportId` |
| `export.completed` | Export Service | Notification | `exportId` |
| `export.failed` | Export Service | Notification | `exportId` |
| `audit.events` | All services | Audit Service | `aggregateId` |
| `notification.events` | Export, Job (optional) | Notification Service | `userId` |
| `*.dlq` | Consumers | Ops / replay tooling | — |

## Communication Patterns

### Synchronous (HTTP via Gateway)
- React → Gateway → Job/Auth/Dashboard/Export/Audit
- Export Service → Job Service (fetch filtered job IDs/data for export)

### Asynchronous (Kafka)
- Job write → Outbox → Kafka → Dashboard materialized views
- Export request → Kafka → background Excel worker
- Failures → DLQ → manual replay

## Failure Handling

- **Outbox Pattern**: DB commit and event publish are atomic; publisher retries on crash.
- **Idempotent consumers**: `eventId` deduplication table in Dashboard/Audit.
- **DLQ**: Poison messages after N retries; alert via metrics.
- **Circuit breaker**: Gateway → downstream on repeated failures (Resilience4j).
- **Optimistic locking**: Concurrent job updates return 409 Conflict.

## CQRS (Practical)

- **Write side**: Job Service + `job_db` (normalized, transactional).
- **Read side**: Dashboard Service + `dashboard_db` (denormalized, event-driven).
- Dashboard may lag seconds behind Job Service — acceptable for analytics.

## Observability Stack

- **Logs**: JSON (Logback) with `correlationId`, `traceId`, `userId`
- **Traces**: OpenTelemetry → Jaeger
- **Metrics**: Micrometer → Prometheus → Grafana

## API Routes (Gateway)

```
/api/auth/**      → auth-service:8081
/api/users/**     → auth-service:8081
/api/jobs/**      → job-service:8082
/api/dashboard/** → dashboard-service:8083
/api/export/**    → export-service:8084
/api/audit/**     → audit-service:8085
```

## Interview Quick Answers

**Why microservices?** Independent scaling (export workers vs job API), team ownership, technology isolation. Trade-off: operational complexity.

**Why not one database?** Coupling, schema change blast radius, can't scale read/write independently.

**Why Kafka?** Decoupling, buffering during spikes, replay, audit trail of facts.

**Why Outbox?** Prevents "DB saved but event lost" — the #1 dual-write bug in event-driven systems.
