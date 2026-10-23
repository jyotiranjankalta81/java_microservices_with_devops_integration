# Job Intelligence Platform

A multi-service job tracker and school evidencing feed. Staff log opportunities, search and filter them, export to Excel, and watch analytics update from Kafka events. The evidencing module is a scoped media feed for school activity: posts, photos and video, comments, and notifications, with role-based access and public or private visibility.

The backend is Java 21 and Spring Boot 3. Each service owns its PostgreSQL database. The React frontend talks only to the API gateway.

## Features

- Register and log in with JWT access through the API gateway. Login and register are public; other routes require a token.
- Create, search, filter, sort, and paginate job records. Concurrent edits use optimistic locking.
- Dashboard analytics built as a CQRS read model. The job service writes through an outbox; the dashboard consumes `job.*` events.
- Asynchronous Excel export of filtered jobs, with completion events for notifications.
- Immutable audit trail consumed from Kafka.
- School evidencing feed: posts, categories, presigned media upload, likes, comments, and in-app notifications. Access is deny-by-default by role, school, branch, and class. Private posts are visible only to the author and tagged recipients.
- Observability: structured logs, Prometheus metrics, Grafana, Loki, and Jaeger traces.

## Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Services | Spring Boot 3.3.5, Spring Cloud Gateway 2023.0.3 |
| Persistence | Spring Data JPA, Flyway, PostgreSQL 16 |
| Messaging | Apache Kafka (KRaft), Spring Kafka |
| Cache | Redis 7 |
| Security | Spring Security, JWT (jjwt) |
| Resilience | Resilience4j circuit breakers on the gateway |
| Export | Apache POI |
| Evidencing media | S3-compatible storage (local filesystem fallback) and SQS-style queues |
| Frontend | React 18, TypeScript, Vite, React Query, Recharts |
| Observability | Micrometer, Prometheus, Grafana, Loki, Promtail, OpenTelemetry, Jaeger |
| Tests | JUnit 5, Mockito |
| Runtime | Docker Compose |

## Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose
- Node.js 20+ (only if you run the frontend in dev mode)

## Quick start

From the repository root:

```bash
mvn clean package -DskipTests
docker compose up --build
```

Compose starts PostgreSQL, Redis, Kafka, the observability stack, every service, and a built frontend.

| What | URL |
|------|-----|
| Frontend | http://localhost:5173 |
| API gateway | http://localhost:8080 |
| Auth Swagger | http://localhost:8081/swagger-ui.html |
| Job Swagger | http://localhost:8082/swagger-ui.html |
| Evidencing Swagger | http://localhost:8087/swagger-ui.html |
| Kafka UI | http://localhost:8090 |
| Grafana | http://localhost:3001 (admin / admin) |
| Jaeger | http://localhost:16686 |
| Prometheus | http://localhost:9090 |

Frontend hot reload (do not run this at the same time as the Compose frontend; both use port 5173):

```bash
cd frontend
npm install
npm run dev
```

Vite proxies `/api/evidencing` to `http://localhost:8087` and other `/api` calls to the gateway on port 8080.

### Register and log in

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@jobintel.com","password":"password123","fullName":"Demo User"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@jobintel.com","password":"password123"}'
```

Use the returned token as `Authorization: Bearer <token>` on later calls.

## Services

Each service has its own database. Services do not query each other's schemas. Compose uses the `dev` Spring profile. Running a service from the IDE defaults to `local`, which expects Postgres, Redis, and Kafka on localhost.

| Service | Port | Database | Role |
|---------|------|----------|------|
| API gateway | 8080 | — | Routes, JWT check, correlation IDs, circuit breakers |
| Auth | 8081 | `auth_db` | Users, login, refresh tokens |
| Job | 8082 | `job_db` | Job CRUD, search, outbox publisher |
| Dashboard | 8083 | `dashboard_db` | Read model for analytics |
| Export | 8084 | `export_db` | Async Excel generation |
| Audit | 8085 | `audit_db` | Consumes `audit.events` |
| Notification | 8086 | — | Consumes notification and export events |
| Evidencing | 8087 | `evidencing_db` | Posts, media, comments, scoped notifications |

Gateway routes:

| Path | Upstream |
|------|----------|
| `/api/auth/**`, `/api/users/**` | Auth |
| `/api/jobs/**` | Job |
| `/api/dashboard/**` | Dashboard |
| `/api/export/**` | Export |
| `/api/audit/**` | Audit |
| `/api/evidencing/**` | Evidencing |

`/api/auth/login`, `/api/auth/register`, and actuator paths are public. Everything else needs a valid JWT.

Postgres is created by `infrastructure/postgres/init-databases.sql`. Local credentials match the Compose environment (`auth_user` / `auth_pass`, and the same pattern for `job`, `dashboard`, `export`, `audit`, and `evidencing`). The superuser is `postgres` / `postgres`.

Kafka topics include `job.created`, `job.updated`, `job.deleted`, `export.requested`, `export.completed`, `export.failed`, `audit.events`, and `notification.events`, plus dead-letter topics. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Frontend

After login the app opens these routes:

| Route | Page |
|-------|------|
| `/dashboard` | Job analytics |
| `/jobs`, `/jobs/new`, `/jobs/:id` | Job list and editor |
| `/exports` | Export history |
| `/evidencing`, `/evidencing/new`, `/evidencing/:id` | Activity feed, composer, post detail |
| `/observability` | Links into the local metrics and traces stack |
| `/profile` | Signed-in user |

## Configuration

Important environment variables (defaults are for local development only):

| Variable | Used by | Default |
|----------|---------|---------|
| `JWT_SECRET` | Gateway, auth, evidencing | A development placeholder. Set a 256-bit secret outside local use. |
| `SPRING_PROFILES_ACTIVE` | All services | `local` from the IDE, `dev` in Compose |
| `KAFKA_BOOTSTRAP` | Job, dashboard, export, audit, notification | `localhost:9092` |
| `REDIS_HOST` / `REDIS_PORT` | Most services | `localhost` / `6379` |
| `*_DB_URL`, `*_DB_USER`, `*_DB_PASSWORD` | Each owning service | See Compose and `application.yml` |
| `AWS_S3_BUCKET`, `AWS_REGION`, `AWS_SQS_*` | Evidencing | Local simulation writes under `./target/evidencing-storage` when no AWS endpoint is set |

## Tests

```bash
mvn test
```

Evidencing authorization scenarios only:

```bash
mvn test -pl services/evidencing-service -am
```

## Project structure

```
job-intelligence-platform/
├── pom.xml                 # Parent POM (Java 21, Spring Boot 3.3.5)
├── docker-compose.yml
├── common/
│   ├── common-core/        # Shared exceptions, correlation IDs, logging
│   ├── common-events/      # Domain events and topic names
│   └── common-security/    # JWT helpers
├── services/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── job-service/
│   ├── dashboard-service/
│   ├── export-service/
│   ├── audit-service/
│   ├── notification-service/
│   └── evidencing-service/
├── frontend/               # React + TypeScript + Vite
├── infrastructure/         # Dockerfiles, Postgres init, Prometheus, Grafana, Promtail
└── docs/
```

## Documentation

| Document | What it covers |
|----------|----------------|
| [docs/TECH_DETAILS.md](docs/TECH_DETAILS.md) | Stack, phases, patterns, source map, how to run |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Service boundaries, Kafka topics, CQRS |
| [docs/DATABASE.md](docs/DATABASE.md) | Schemas, indexes, Flyway |
| [docs/FAILURE_SCENARIOS.md](docs/FAILURE_SCENARIOS.md) | Production failure walkthroughs |
| [services/evidencing-service/README.md](services/evidencing-service/README.md) | Roles, visibility rules, media pipeline, evidencing API |
