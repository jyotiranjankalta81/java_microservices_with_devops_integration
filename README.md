# Job Intelligence Platform

Enterprise-grade Job Tracker built with Java 21, Spring Boot 3, Kafka, PostgreSQL, and React.

## Quick Start

```bash
# Build all services
mvn clean package -DskipTests

# Start infrastructure + all services
docker compose up --build

# Frontend dev (optional)
cd frontend && npm install && npm run dev
```

| Service | Port | Swagger |
|---------|------|---------|
| API Gateway | 8080 | — |
| Auth | 8081 | /swagger-ui.html |
| Job | 8082 | /swagger-ui.html |
| Dashboard | 8083 | — |
| Export | 8084 | — |
| Audit | 8085 | — |
| Frontend | 5173 | — |
| Kafka UI | 8090 | — |
| Grafana | 3001 | admin/admin |
| Jaeger | 16686 | — |
| Prometheus | 9090 | — |

## Documentation

| Document | Description |
|----------|-------------|
| **[docs/TECH_DETAILS.md](docs/TECH_DETAILS.md)** | **Complete technical reference** — business requirements, all 20 phases, stack, patterns, failure scenarios (inline), HikariCP, OpenAPI, SOLID, source files, Maven versions, interview Q&A, how to run |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Service boundaries, Kafka topics, CQRS |
| [docs/DATABASE.md](docs/DATABASE.md) | Schema, indexes, Flyway migration strategy |
| [docs/FAILURE_SCENARIOS.md](docs/FAILURE_SCENARIOS.md) | 25 production failure walkthroughs |

## Architecture

See [docs/TECH_DETAILS.md](docs/TECH_DETAILS.md) for the complete phase-by-phase guide.

## Register & Login

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@jobintel.com","password":"password123","fullName":"Demo User"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@jobintel.com","password":"password123"}'
```

## Project Structure

```
job-intelligence-platform/
├── common/           # Shared libraries (core, events, security)
├── services/         # Microservices
├── frontend/         # React + TypeScript
├── infrastructure/   # Docker, Prometheus, Grafana
└── docs/             # Architecture & interview guides
```

## Interview Topics Covered

Spring Boot, JPA, Flyway, Kafka, Outbox Pattern, CQRS, JWT, API Gateway, HikariCP, Resilience4j, Testcontainers, Docker, OpenTelemetry, Prometheus, structured logging, optimistic locking, dynamic JPA Specifications.
