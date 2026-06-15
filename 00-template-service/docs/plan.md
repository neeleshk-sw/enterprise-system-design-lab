# 00-template-service — Implementation Plan & Roadmap

> **Status:** Planning. No code yet. This document is the single reference for building the template service and is meant to be consulted throughout implementation. It captures the design, the contracts every copied project inherits, and a checkpoint roadmap (CP0–CP7) to track progress.

---

## 1. Purpose & Scope

`00-template-service` is the **baseline every other project in the lab is copied from**. It must be consistent, simple, extensible, and production-ready — and nothing more. It demonstrates a complete, correct **Controller → Service → Repository → Database** slice using the `Customer` domain.

**Intentionally excluded** (these belong to their own later sections, never the template): Kafka, RabbitMQ, Redis, OpenSearch, Kubernetes, OAuth2/security providers, AWS integrations. The template stays pure Spring Boot + PostgreSQL + Flyway + Actuator + OpenAPI + Docker + tests.

**Design priorities (in order):** Consistency → Simplicity → Extensibility → Production-readiness.

---

## 2. Module & Directory Layout

Maven **multi-module** build with a parent aggregator POM. The user's high-level tree (flat `src/`, `Dockerfile`, `pom.xml` at root) is mapped onto the multi-module structure as follows: the parent `pom.xml`, `docs/`, `scripts/`, `docker/`, `docker-compose.yml`, and `README.md` live at the `00-template-service` root; application `src/` and the service `Dockerfile` live inside the `customer-service` module.

```
00-template-service/
├── pom.xml                      # parent / aggregator POM (packaging: pom)
├── README.md
├── docker-compose.yml           # Postgres + customer-service
├── docker/                      # supporting infra config (init scripts, env, etc.)
├── scripts/                     # helper scripts (build.sh, run.sh, db reset, etc.)
├── docs/
│   ├── plan.md                  # this file
│   ├── architecture.md          # architecture + diagram (to be written)
│   ├── api-spec.md              # endpoint contract / OpenAPI summary
│   └── setup-guide.md           # how to run locally & in Docker
│
├── common-library/              # MODULE: shared reusable jar (no @SpringBootApplication)
│   ├── pom.xml
│   └── src/main/java/com/enterprise/common/...
│
└── customer-service/            # MODULE: runnable Spring Boot application
    ├── pom.xml
    ├── Dockerfile               # multi-stage build
    ├── src/main/java/com/enterprise/customer/...
    ├── src/main/resources/
    │   ├── application.yml
    │   ├── application-local.yml
    │   ├── application-docker.yml
    │   └── db/migration/V1__create_customer_table.sql
    └── src/test/java/com/enterprise/customer/...
```

**POM relationships:** parent declares Java 21, Spring Boot 3.x BOM, shared plugin/versions, and lists both modules. `customer-service` depends on `common-library`.

---

## 3. Package Structure

### 3.1 `customer-service` → `com.enterprise.customer`

```
com.enterprise.customer
├── CustomerServiceApplication        # @SpringBootApplication entry point
├── config                            # Spring config (OpenAPI, web/logging, JPA auditing)
├── controller                        # thin REST controllers, no business logic
├── service
│   ├── (interfaces, e.g. CustomerService)
│   ├── impl                          # service implementations (@Transactional here)
│   └── mapper                        # entity <-> DTO mapping (CustomerMapper)
├── repository                        # Spring Data JPA repositories (Optional returns)
├── entity                            # JPA entities (Customer extends AuditEntity)
├── dto                               # CustomerRequest, CustomerResponse
├── exception                         # domain-specific exceptions (if any beyond common)
├── validation                        # custom validators / Bean Validation annotations
├── util                              # service-local helpers
└── common                            # service-local shared bits not promoted to the library
```

> Cross-project reusable concerns live in `common-library`; the service's `common`/`util`/`exception` packages hold only what is specific to this service. When copying the template, the service keeps depending on the unchanged `common-library`.

### 3.2 `common-library` → `com.enterprise.common`

```
com.enterprise.common
├── response
│   ├── ApiResponse<T>                # success wrapper
│   └── ErrorResponse                 # unified error body
├── exception
│   ├── BaseException                 # parent of all custom exceptions
│   ├── ResourceNotFoundException
│   ├── BusinessException
│   ├── ValidationException
│   └── GlobalExceptionHandler        # @RestControllerAdvice
├── entity
│   └── AuditEntity                   # @MappedSuperclass auditing base
├── constant
│   └── Constants                     # shared constants (error codes, headers, etc.)
└── util                              # reusable utility classes
```

---

## 4. Cross-Cutting Contracts

These are the inherited contracts every copied project gets for free. They are the heart of the template's consistency guarantee.

### 4.1 Success wrapper — `ApiResponse<T>`

Every successful response is wrapped:

```json
{
  "success": true,
  "data": { },
  "message": "Customer created successfully"
}
```

Provide static factories, e.g. `ApiResponse.success(data, message)` and `ApiResponse.success(data)`.

### 4.2 Unified error body — `ErrorResponse`

> **Reconciliation:** the spec gave two error shapes — `{timestamp, code, message, path}` and `{success:false, errorCode, message}`. They are merged into one canonical `ErrorResponse`:

```json
{
  "success": false,
  "errorCode": "CUSTOMER_NOT_FOUND",
  "message": "Customer not found",
  "timestamp": "2026-06-11T10:15:30Z",
  "path": "/api/v1/customers/42"
}
```

`timestamp` is ISO-8601 UTC; `path` is the request URI; `errorCode` is a stable machine-readable code from `Constants`.

### 4.3 Exception hierarchy & handler mapping

All custom exceptions extend `BaseException` (carries `errorCode` + `message`). `GlobalExceptionHandler` (`@RestControllerAdvice`) is the **only** place exceptions become HTTP responses — controllers never catch.

| Exception | HTTP status | errorCode (example) |
|---|---|---|
| `ResourceNotFoundException` | 404 Not Found | `RESOURCE_NOT_FOUND` / `CUSTOMER_NOT_FOUND` |
| `ValidationException` | 400 Bad Request | `VALIDATION_ERROR` |
| `MethodArgumentNotValidException` (Bean Validation) | 400 Bad Request | `VALIDATION_ERROR` (field details aggregated) |
| `BusinessException` | 409 Conflict (or 422) | `BUSINESS_RULE_VIOLATION` |
| `BaseException` (fallback) | 400 Bad Request | from exception |
| `Exception` (uncaught) | 500 Internal Server Error | `INTERNAL_ERROR` |

### 4.4 Structured logging

- **SLF4J only — `System.out`/`println` is banned.**
- A request-logging filter/interceptor (registered in `config`) logs per request: **TraceId, Request URI, Response Status, Execution Time (ms)**.
- TraceId is generated per request (or read from an incoming header), stored in **MDC**, echoed in a response header, and included in the unified error path where useful.
- Logback configured for structured (JSON-capable) output so logs are aggregation-ready in later observability sections.

### 4.5 `AuditEntity`

`@MappedSuperclass` with JPA auditing (`@EnableJpaAuditing` in `config`):

| Field | Annotation |
|---|---|
| `createdAt` (Instant) | `@CreatedDate` |
| `updatedAt` (Instant) | `@LastModifiedDate` |
| `version` (Long) | `@Version` (optimistic locking, optional but recommended) |

`Customer` extends `AuditEntity`. Per code style: `@Data` is used on **DTOs only**, never on JPA entities (avoid hashCode/equals pitfalls) — entities use explicit/`@Getter`/`@Setter` as needed.

---

## 5. Persistence, Config & Ops

### 5.1 Flyway & schema

- All schema changes go through Flyway migrations in `src/main/resources/db/migration/`.
- Naming convention: `V<n>__<description>.sql`, first migration `V1__create_customer_table.sql`.
- **No auto-DDL:** `spring.jpa.hibernate.ddl-auto=validate` (Hibernate validates against the Flyway-managed schema, never generates it).

### 5.2 Configuration profiles

| Profile | DB target | Use |
|---|---|---|
| `local` | Postgres on `localhost:5432` | developer machine |
| `docker` | Postgres service hostname | inside docker-compose |
| `test` | Testcontainers Postgres | integration tests |

Externalized config (Twelve-Factor): DB URL/credentials via environment variables with sane local defaults.

### 5.3 Actuator & OpenAPI

- Spring Boot Actuator exposes **health, info, metrics** (Micrometer registry in place for later Prometheus wiring — but no Prometheus dependency in the template).
- OpenAPI via **springdoc**: Swagger UI for interactive docs; generated spec summarized in `docs/api-spec.md`.

### 5.4 Reference API (Customer)

`/api/v1/customers` — `POST` create, `GET /{id}` fetch, `GET` list (paged), `PUT /{id}` update, `DELETE /{id}`. All requests/responses use DTOs + `ApiResponse<T>`; validation via Bean Validation on `CustomerRequest`.

---

## 6. Testing Strategy

| Layer | Type | Tools | What |
|---|---|---|---|
| Service | **Unit** | JUnit 5 + Mockito | business logic in `service/impl` with mocked repository/mapper |
| Repository | **Integration** | JUnit 5 + Testcontainers (Postgres) | real queries against a real Postgres, Flyway-migrated |
| Controller | **Integration** | JUnit 5 + `@SpringBootTest`/MockMvc (+ Testcontainers) | end-to-end request → response, wrapper & error shapes asserted |

- Integration tests require Docker (Testcontainers).
- Tests assert the unified `ApiResponse`/`ErrorResponse` contracts and the exception→status mapping.

---

## 7. Docker & Local Run

- **Dockerfile** (in `customer-service/`): multi-stage (Maven build stage → slim JRE runtime), non-root user, exposes app port, runs the jar.
- **docker-compose.yml** (root): `postgres` service + `customer-service` (profile `docker`), with healthchecks and a named volume for Postgres data.
- **scripts/**: convenience wrappers — build, run, reset-db — so the template is turnkey.
- Run locally: `docker compose up -d` (Postgres) → `mvn spring-boot:run -pl customer-service`. Full stack: `docker compose up --build`.

---

## 8. Copy-to-New-Project Recipe

To start any later project from this template:

1. Copy the `00-template-service` tree into `NN-section/<project>/`.
2. Find/replace base package `com.enterprise.customer` → `com.enterprise.<project>`.
3. Rename artifact/module IDs and the application class.
4. Replace the `Customer` domain (entity, DTOs, mapper, repository, service, controller) with the project's domain.
5. Reset Flyway: replace `V1__create_customer_table.sql` with the project's `V1__...`.
6. Update DB/compose names, `application.yml`, and the four docs.
7. Keep `common-library` unchanged (or copy as-is); it is the stable shared foundation.

---

## 9. Roadmap & Checkpoints

Each checkpoint is a verifiable milestone. Build in order; do not advance until the checkpoint's "Done when" holds.

### CP0 — Build skeleton
Parent POM + `common-library` + `customer-service` modules with empty package structure.
**Done when:** `mvn clean validate` (or `compile`) succeeds for the aggregator; both modules recognized.

### CP1 — common-library foundation
`ApiResponse`, `ErrorResponse`, `Constants`, `BaseException` + `ResourceNotFoundException`/`BusinessException`/`ValidationException`, `GlobalExceptionHandler`, `AuditEntity`, utilities.
**Done when:** library compiles; unit tests for response factories and the error-mapping logic pass; `mvn -pl common-library test` green.

### CP2 — Domain & persistence
`Customer` entity (extends `AuditEntity`), `CustomerRepository` (Optional returns), Flyway `V1__create_customer_table.sql`, JPA auditing config, `ddl-auto=validate`.
**Done when:** repository **integration test** (Testcontainers Postgres) passes; Flyway migrates cleanly.

### CP3 — Service layer
`CustomerService` + `impl` (`@Transactional`), `CustomerMapper`, `CustomerRequest`/`CustomerResponse`, validation rules.
**Done when:** **unit tests** (Mockito) cover create/get/list/update/delete incl. not-found path; green.

### CP4 — Controller layer
`CustomerController` (thin), request validation, responses wrapped in `ApiResponse`.
**Done when:** **controller integration tests** assert success wrapper, paging, and error bodies for 400/404/409.

### CP5 — Cross-cutting wiring
Structured-logging filter (TraceId/URI/status/exec-time via MDC + SLF4J), `GlobalExceptionHandler` active across the app, OpenAPI/Swagger UI, Actuator health/info/metrics.
**Done when:** hitting endpoints shows wrapped responses, structured logs with all four fields, Swagger UI loads, `/actuator/health|info|metrics` respond.

### CP6 — Containerization & end-to-end
Multi-stage `Dockerfile`, `docker-compose.yml` (Postgres + service), helper scripts.
**Done when:** `docker compose up --build` brings the stack up healthy; a smoke create+fetch against the container succeeds.

### CP7 — Docs & Definition of Done
`architecture.md` (+ diagram), `api-spec.md`, `setup-guide.md`, root `README.md`; final DoD pass.
**Done when:** Section 10 checklist fully ticked.

---

## 10. Definition of Done (template)

- [ ] Multi-module build: `mvn clean verify` green from `00-template-service/`
- [ ] `common-library` provides `ApiResponse`, `ErrorResponse`, `Constants`, `BaseException` (+3 exceptions), `GlobalExceptionHandler`, `AuditEntity`, utilities
- [ ] No entity ever exposed by the API — only `CustomerRequest`/`CustomerResponse`
- [ ] Constructor injection everywhere (no field `@Autowired`); controllers thin; `@Transactional` only in service impl
- [ ] Repositories return `Optional`, never null
- [ ] All errors flow through `GlobalExceptionHandler` and render the unified `ErrorResponse`
- [ ] Schema only via Flyway; `ddl-auto=validate` (no auto-DDL)
- [ ] Structured logging via SLF4J with TraceId, Request URI, Response Status, Execution Time; zero `System.out`
- [ ] Actuator exposes health/info/metrics; OpenAPI/Swagger UI available
- [ ] Unit tests on service layer; integration tests on controller + repository (Testcontainers) — all passing
- [ ] `Dockerfile` + `docker-compose.yml` run the stack end-to-end
- [ ] `docs/architecture.md`, `docs/api-spec.md`, `docs/setup-guide.md`, and `README.md` complete
- [ ] Copy-to-new-project recipe verified to produce a building project
