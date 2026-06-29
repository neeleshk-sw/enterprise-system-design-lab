# 01-foundation

Core application architecture, taught as a **didactic progression**. Each project adds one layer of structure on top of the last, so reading them in order traces the path from the simplest correct service to a polished, well-structured API.

```
basic-crud  →  layered-architecture  →  clean-architecture  →  multi-module  →  rest-api-best-practices
 simplest        proper layering         framework-free core    physical split      API polish
```

Every project is **independent and runnable on its own** (own `docker-compose.yml`, own database, port `8080`). All target Java 21 / Spring Boot 3.5, PostgreSQL + Flyway (`ddl-auto=validate`), and ship unit + integration (Testcontainers) tests, Docker support, and a README. See [`docs/plan.md`](docs/plan.md) for the full section design.

## The projects

### 1. [`basic-crud`](basic-crud) — the simplest correct CRUD (Customer)
The bare minimum *done right*: thin controller → service → repository → DB, DTOs, Bean Validation, correct status codes (201+`Location`, 204, 404, 400). Deliberately lean — **single Maven module, no shared library, plain DTO responses** (no envelope), and a minimal inline error handler. The baseline a reader meets first.

### 2. [`layered-architecture`](layered-architecture) — layering + DTO mapping (Product / Category)
Re-establishes the full template structure: **multi-module** (`common-library` + `product-service`) with the shared `ApiResponse`/`ErrorResponse` envelopes, a dedicated mapper, and business rules in the service (unique SKU → 409, missing category → 404). Shows *why* each layer exists and where logic belongs.

### 3. [`clean-architecture`](clean-architecture) — ports & adapters (Order)
Inverts the dependencies: a **framework-free `domain` and `application`** (use-cases + ports), with Spring/JPA confined to adapters (web in, persistence out). Use-cases are wired as beans so the core carries no Spring annotations. The dependency rule is **enforced by an ArchUnit test**, not just documented.

### 4. [`multi-module`](multi-module) — physical Maven split (Customer)
The same boundaries as clean-architecture, but enforced **physically** by the Maven reactor: `domain`, `persistence`, `api`, `app` (+ `common-library`). The repository **port** is declared in `domain` and fulfilled by Spring Data in `persistence`; only `app` is runnable. You literally cannot compile an upward dependency.

### 5. [`rest-api-best-practices`](rest-api-best-practices) — API design concerns (Product)
Layers on the qualities a production API needs: **URI versioning**, **pagination & sorting**, **filtering** (JPA Specifications), **HATEOAS-lite** navigation links, **idempotency keys** (`Idempotency-Key` header + dedupe table), and **ETag** conditional GETs (`If-None-Match` → 304).

## Two views of the same discipline

`clean-architecture` and `multi-module` both keep infrastructure out of the core, from different angles:

| | Enforcement | Packaging |
|---|---|---|
| `clean-architecture` | package-level dependency rule, verified by **ArchUnit** | single deployable module |
| `multi-module` | **compile-time**, via separate Maven modules | one artifact per concern |

## Running any project

```bash
cd <project>            # e.g. cd clean-architecture
docker compose up --build        # full stack on :8080
# or: docker compose up -d postgres && mvn spring-boot:run   (multi-module: -pl app -am)
mvn clean verify                 # build + unit + integration tests (Docker required)
```

Target JDK is 21 — if your `mvn` defaults to a newer JDK, set `JAVA_HOME` to a JDK 21.

## Build order

Each project stands alone, but `basic-crud → layered-architecture → clean-architecture → multi-module → rest-api-best-practices` tells the clearest story (simple → complex).
