# Production Failure Scenarios

For each scenario: **what happens**, **why**, **detection**, **recovery**, **debugging**.

---

## 1. Duplicate Job Creation

**Layman:** User double-clicks "Save" or retries after timeout.

**What happens:** Unique index `uq_jobs_user_url` rejects second insert with same URL → `409 DUPLICATE_RESOURCE`.

**Detection:** HTTP 409, DB constraint violation logs, metric `http.server.requests{status=409}`.

**Recovery:** Client shows "already exists"; idempotent POST could use client-generated idempotency key (future enhancement).

**Debug:** Search logs by `correlationId` from response header; check `jobs` table for existing row.

---

## 2. Kafka Message Duplication

**Layman:** Kafka guarantees at-least-once; same event may arrive twice.

**What happens:** Dashboard/Audit check `processed_events.event_id` / `audit_records.event_id` and skip duplicates.

**Detection:** Debug log "Skipping duplicate event".

**Recovery:** Automatic — idempotent consumer.

**Debug:** Compare `eventId` in Kafka message vs `processed_events` table.

---

## 3. Kafka Unavailable

**Layman:** Message bus is down; jobs still save but events queue in outbox.

**What happens:** Job CRUD succeeds; outbox rows stay `PENDING`. OutboxPublisher retries every 2s when Kafka returns.

**Detection:** Growing `outbox_events` pending count; Kafka consumer lag alerts.

**Recovery:** Restore Kafka; publisher drains outbox automatically.

**Debug:** `SELECT count(*) FROM outbox_events WHERE status='PENDING'`.

---

## 4. Database Unavailable

**What happens:** HikariCP cannot get connection → `503`/`500`, circuit breaker may open at gateway.

**Detection:** Actuator health DOWN, Hikari pool metrics, connection timeout logs.

**Recovery:** Restore PostgreSQL; pods restart; pool refills.

**Debug:** Check `hikaricp.connections.active` vs `maximum-pool-size`.

---

## 5. Connection Pool Exhausted

**Layman:** All DB connections are busy; new requests wait then fail.

**What happens:** Threads block on `getConnection()` until timeout.

**Detection:** Micrometer `hikaricp.connections.pending`, slow HTTP latency.

**Recovery:** Fix slow queries, reduce transaction scope, increase pool (temporary), scale replicas.

**Debug:** Thread dump + active query log; find long `@Transactional` methods.

---

## 6. Slow Database Query

**What happens:** Filter query without index scans full table; request times out.

**Detection:** PG `pg_stat_statements`, trace span duration on repository call.

**Recovery:** Add index (see DATABASE.md), paginate, cache hot aggregates in Dashboard.

**Debug:** `EXPLAIN ANALYZE` on generated SQL; enable Hibernate SQL logging in dev.

---

## 7. Dashboard Temporarily Behind Job Service

**Layman:** Analytics page shows yesterday's count for a few seconds.

**What happens:** CQRS read model updates asynchronously via Kafka.

**Detection:** Compare Job Service count vs Dashboard count; consumer lag metric.

**Recovery:** Self-heals when consumer catches up; optional reconciliation job.

**Debug:** Kafka UI → consumer group lag for `dashboard-service`.

---

## 8–10. Export / Excel Failures

**What happens:** Export worker catches exception → status `FAILED`, publishes `export.failed`.

**Detection:** Export history UI, Kafka topic, logs with export UUID.

**Recovery:** User retries export; ops replays from `export.requested` if needed.

**Debug:** Trace export UUID through logs and `export_jobs.error_message`.

---

## 11. Poison Kafka Message

**What happens:** Deserialization fails repeatedly → message goes to DLQ (configure `DefaultErrorHandler` with DLQ in production).

**Detection:** Consumer retry metrics, DLQ topic depth alert.

**Recovery:** Fix schema, replay corrected message, skip bad offset manually.

**Debug:** Inspect raw message in DLQ; compare with `DomainEvent` schema.

---

## 12–13. Schema Migration / Backward Compatibility

**What happens:** Flyway runs V3 adding nullable `salary_min`; old app ignores column; new app uses it.

**Detection:** Flyway schema history table; deploy pipeline gates.

**Recovery:** Expand-and-contract — never drop columns in same release as code depending on them.

**Debug:** `flyway_schema_history` + application version tag in logs.

---

## 14–15. Concurrent Updates / Optimistic Lock

**What happens:** Two tabs edit same job; second save gets `409 OPTIMISTIC_LOCK_CONFLICT`.

**Detection:** HTTP 409, Hibernate `StaleObjectStateException` in logs (handled).

**Recovery:** User refreshes, merges changes, retries with new `version`.

**Debug:** Compare `version` in request vs DB row.

---

## 16–19. Timeouts, Retry Storms, Circuit Breaker

**What happens:** Gateway/Resilience4j opens circuit after failures; fast-fail instead of hanging.

**Detection:** `resilience4j_circuitbreaker_state`, elevated 503 rate.

**Recovery:** Circuit half-opens after wait duration; fix root cause first.

**Debug:** Trace shows which hop timed out; never retry non-idempotent POST without idempotency key.

---

## 20–25. Restarts, Partial Failures, Outbox Crash

**Outbox publisher crash mid-batch:** Unpublished events remain `PENDING`; next scheduler run republishes. Duplicate Kafka messages handled by idempotent consumers.

**Network failure after DB commit:** Outbox ensures event is never lost — it's in same transaction as job row.

**Debug playbook:** Always start with `X-Correlation-Id` → logs → traceId in Jaeger → Kafka offset → DB state.
