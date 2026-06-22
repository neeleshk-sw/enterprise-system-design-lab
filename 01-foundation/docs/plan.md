# 01-foundation — Section Plan & Roadmap

> **Status:** Planning. No code yet. This document expands the master plan's one-line `01-foundation` entry (`workspace/docs/plan/plan.md` §5) into an executable, per-project design. It is the reference to consult while building each project in this section.

---

## 1. Purpose & Scope

`01-foundation` teaches **core application architecture** as a deliberate **didactic progression** — each project adds one layer of structure on top of the last, so a reader can follow the arc from the simplest possible service to a polished, well-structured API:

```
basic-crud            → the simplest correct CRUD service (single module, near-scratch)
layered-architecture  → proper Controller→Service→Repository layering + DTO mapping (≈ the template)
clean-architecture    → ports & adapters, the dependency rule (domain free of frameworks)
multi-module          → physical Maven module split enforcing boundaries at build time
rest-api-best-practices → API design polish: versioning, paging/filtering, HATEOAS, idempotency, ETags
```

**Relationship to the template:** `00-template-service` is already a clean, layered Customer CRUD with `common-library`. To avoid redundancy, `basic-crud` is intentionally *simpler* than the template, and `layered-architecture` is the project that re-establishes the full template-style structure. Projects 2–5 derive from the template; `basic-crud` is standalone (see §6, reconciliation 2).

**Intentionally excluded** (later sections own these): messaging, caching, NoSQL, search, security providers, Kubernetes, AWS. Foundation stays pure Spring Boot + PostgreSQL + Flyway + tests + Docker.

---

## 2. Section-Wide Conventions

- **Derivation model:** 4 of 5 projects start from the template via the copy recipe (master plan §7). `basic-crud` is built lean and standalone (no `common-library`).
- **Package naming:** `com.enterprise.<project>` — e.g. `com.enterprise.basiccrud`, `com.enterprise.layered`, `com.enterprise.cleanarch`, `com.enterprise.multimodule`, `com.enterprise.restapi`. (This supersedes the master plan's `com.esdl.*`, per reconciliation 1.)
- **Independence:** each project has its own POM(s), `docker-compose.yml`, DB name, and docs; no cross-project dependencies. Run one at a time on the standard ports (app `8080`, Postgres `5432`).
- **Inherited contracts** (for the template-derived projects): `ApiResponse<T>`, unified `ErrorResponse`, `BaseException` hierarchy + `GlobalExceptionHandler`, `AuditEntity`, structured request logging, `ddl-auto=validate` + Flyway. See `00-template-service/docs/plan.md` §4.
- **Code style** (all projects): constructor injection only; thin controllers; `@Transactional` in the service layer only; entities never exposed (DTOs always); repositories return `Optional`; SLF4J only (no `System.out`).
- **Docs contract:** every project ships the 9 artifacts and a README per master plan §4.4; each is verified against the per-project DoD (§5) which builds on master plan §4.5.

---

## 3. Per-Project Specifications

### 3.1 `basic-crud` — the simplest correct CRUD (Customer)

- **Objective:** REST CRUD and correct HTTP semantics from near-scratch, with the least possible structure.
- **Uniquely demonstrates:** the bare minimum done right — resource modelling, status codes (201/200/204/404/400), DTO in/out, Bean Validation, a *minimal* error handler. Deliberately omits the `ApiResponse` envelope and `common-library` so the contrast with `layered-architecture` is visible.
- **Domain & entities:** `Customer` (id, firstName, lastName, email). One table.
- **Structure:** **single Maven module** (no parent aggregator, no `common-library`). Packages: `controller`, `service`, `repository`, `entity`, `dto`, `exception` (a small inline `@RestControllerAdvice` returning a plain `{timestamp,status,error,message,path}` body), `config`.
- **Persistence:** PostgreSQL + Flyway `V1__create_customer_table.sql`, `ddl-auto=validate` (consistency with the stack; the simplicity is structural, not in dropping Flyway).
- **API:** `/api/v1/customers` — POST/GET{id}/GET(list)/PUT/DELETE; returns plain DTOs (no envelope).
- **Tests:** service unit (Mockito); controller + repository integration (Testcontainers).
- **Done when:** `mvn clean verify` green; full CRUD works end-to-end; 404/400 handled by the inline advice; Docker compose runs.

### 3.2 `layered-architecture` — layering + DTO mapping (Product)

- **Objective:** make the Controller → Service → Repository separation and the DTO-mapping boundary the explicit lesson.
- **Uniquely demonstrates:** why each layer exists, where business logic lives, and how the mapper keeps entities out of the API — using the inherited `ApiResponse`/`ErrorResponse` envelopes.
- **Domain & entities:** `Product` (id, name, sku [unique], price, status) + `Category` (id, name); product → category association. Richer than Customer so the service layer has real work (SKU uniqueness, price validation, category lookup).
- **Structure:** derives from the template — parent POM + `common-library` + `product-service` (`com.enterprise.layered`), packages exactly as the template (`controller/service{impl,mapper}/repository/entity/dto/...`).
- **Persistence:** Postgres + Flyway (`products`, `categories`), `validate`.
- **API:** `/api/v1/products`, `/api/v1/categories` — CRUD wrapped in `ApiResponse`.
- **Tests:** service unit (Mockito); controller + repository integration (Testcontainers); assert envelopes and 404/409 (duplicate SKU).
- **Done when:** build green; layering + mapping demonstrated; envelopes correct; duplicate-SKU → 409.

### 3.3 `clean-architecture` — ports & adapters, dependency rule (Order)

- **Objective:** the Clean Architecture dependency rule — domain and use-cases independent of frameworks; infrastructure depends inward.
- **Uniquely demonstrates:** a framework-free domain, use-case interactors behind input ports, and infrastructure (web, JPA) behind output ports — the inverse of the layered project's framework-centric layering.
- **Domain & entities:** `Order`, `OrderItem`, `OrderStatus`, total calculation and simple invariants — as **pure POJOs** (no Spring/JPA imports).
- **Structure (single deployable module):**
  - `domain` — entities, value objects, domain rules (no framework deps).
  - `application` — `usecase` (interactors) + `port` (input port interfaces, output/gateway interfaces).
  - `adapter` — `web` (controllers calling use-cases), `persistence` (JPA entities + repository implementing the output port; domain↔JPA mapping).
  - `config` — Spring wiring of use-cases and adapters.
  - Package root `com.enterprise.cleanarch`.
- **Persistence:** Postgres + Flyway (`orders`, `order_items`); JPA entities live only in the persistence adapter, separate from the domain model.
- **Dependency rule enforcement:** **ArchUnit** test asserting `domain`/`application` have no Spring/JPA dependencies (makes the rule observable, satisfying the DoD).
- **Tests:** domain & use-case unit tests (pure, no Spring); web + persistence integration (Testcontainers); ArchUnit rule test.
- **Done when:** build green; ArchUnit passes; use cases tested without a Spring context.

### 3.4 `multi-module` — physical Maven module split (Customer)

- **Objective:** enforce architectural boundaries **physically** through Maven modules and the reactor build.
- **Uniquely demonstrates:** module decomposition and dependency direction enforced at build time (you literally cannot import upward) — contrast with `clean-architecture`, which enforces the same discipline at the *package* level inside one module.
- **Structure (parent + modules):**
  - `domain` — entities + domain logic, minimal deps.
  - `persistence` — JPA repositories; depends on `domain`.
  - `api` — DTOs, controllers, mappers; depends on `domain`.
  - `app` — Spring Boot bootstrap + config; depends on `api` + `persistence`; the only runnable module.
  - plus a copied `common-library`.
  - Packages `com.enterprise.multimodule.{domain,persistence,api,app}`.
- **Domain & entities:** `Customer` (reuse the simple template domain) — keeps focus on the split, not the domain.
- **Persistence:** Postgres + Flyway (migrations in `persistence` or `app` resources), `validate`.
- **Tests:** per-module unit tests; integration (Testcontainers) in `persistence`/`app`.
- **Done when:** reactor builds in dependency order; `app` runs end-to-end; upward dependencies are impossible by construction.

### 3.5 `rest-api-best-practices` — API design polish (Product)

- **Objective:** demonstrate production-grade REST API concerns on top of a solid layered service.
- **Uniquely demonstrates:**
  - **URI versioning** (`/api/v1/...`; documented path to `v2`),
  - **pagination** (`page`/`size`/`sort`) + **filtering & sorting** via query params,
  - the **unified error format** (inherited),
  - **HATEOAS-lite** (self/next/prev links in responses),
  - **idempotency keys** — `Idempotency-Key` header on POST, persisted in a dedupe table so retries return the original result without creating duplicates,
  - **conditional requests** — `ETag` / `If-None-Match` → `304 Not Modified`.
- **Domain & entities:** `Product`; plus an `idempotency_keys` table.
- **Structure:** derives from the template (`com.enterprise.restapi`).
- **Tests:** integration tests asserting versioned routes, paging/filtering, ETag `304`, and idempotent POST (same key → same body, single row created).
- **Done when:** build green; each API concern demonstrated and covered by a test.

---

## 4. Build Order (within the section)

Simple → complex, so each project builds on concepts from the previous:

1. `basic-crud` → 2. `layered-architecture` → 3. `clean-architecture` → 4. `multi-module` → 5. `rest-api-best-practices`

Each is independent and could be built in isolation, but this order tells the clearest story.

---

## 5. Definition of Done (per project)

Baseline (from master plan §4.5) for every project:

- [ ] `mvn clean verify` green; runs end-to-end via `docker compose up` + the app
- [ ] Flyway migrations own the schema; `ddl-auto=validate`
- [ ] Swagger/OpenAPI exposed; Actuator health/info/metrics (template-derived projects)
- [ ] Unit tests (service/domain) + integration tests (controller/repository, Testcontainers) passing
- [ ] Architecture diagram + README per the documentation contract
- [ ] Constructor injection; thin controllers; DTOs only; `Optional` from repositories; SLF4J only

Project-specific additions:

- `basic-crud` — single-module build; inline error handler returns the documented plain error body; no `common-library` dependency.
- `layered-architecture` — `ApiResponse`/`ErrorResponse` envelopes; duplicate-SKU → 409.
- `clean-architecture` — **ArchUnit** dependency-rule test green; domain/use-cases compile without Spring/JPA on the classpath path.
- `multi-module` — multi-module reactor builds; only `app` is runnable; module dependency directions correct.
- `rest-api-best-practices` — tests prove versioning, pagination/filtering, ETag `304`, and idempotent POST.

---

## 6. Reconciliations & Notes

1. **Package convention.** Use `com.enterprise.<project>` (as established when building the template). The master plan's `com.esdl.<section>.<project>` (§4.2) is superseded across the lab.
2. **`basic-crud` deviates from "always derive from template."** This is intentional (the didactic-progression choice): it is single-module, has no `common-library`, and returns plain DTOs/error bodies. Every other foundation project obeys the derive-from-template rule.
3. **`clean-architecture` vs `multi-module`.** Both are about boundaries, but at different levels: clean-architecture enforces the dependency rule at the **package** level within one deployable (and verifies it with ArchUnit); multi-module enforces boundaries **physically** via separate Maven modules. Keep both — they teach the same discipline from different angles.

> Each project will get its own `README.md` (and may add an `architecture.md`) under its directory; this section-level `docs/` may also collect cross-project notes, mirroring `00-template-service/docs/`.
