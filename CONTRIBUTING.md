# Contributing

This repository is a collection of **independent, self-contained projects**. Consistency across them is the whole point, so every project follows the same rules.

## Starting a new project

1. Copy `00-template-service` into the target section: `NN-section/<project>/`.
2. Find/replace the base package `com.enterprise.customer` → `com.enterprise.<project>`; rename artifact ids and the application class.
3. Replace the Customer domain (entity, DTOs, mapper, repository, service, controller) and the Flyway `V1__*.sql` with the project's own.
4. Add only the infrastructure this project needs to its `docker-compose.yml`.
5. Keep `common-library` unchanged — it is the shared foundation.

## Conventions (inherited from the template)

- **Layered:** Controller → Service → Repository → Database. Controllers are thin; `@Transactional` lives in the service layer only.
- **DTO-only API:** never expose JPA entities; map to request/response DTOs.
- **Constructor injection** everywhere (no field `@Autowired`).
- **Repositories return `Optional`**, never null.
- **All errors** flow through the global exception handler and render the unified `ErrorResponse`.
- **Schema via Flyway**; Hibernate runs with `ddl-auto=validate` (no auto-DDL).
- **SLF4J** structured logging (trace id, URI, status, execution time); no `System.out`.

## Definition of Done

A project is done when: `mvn clean verify` is green; it runs end-to-end via `docker compose up`; it has unit tests (service) and integration tests (controller + repository, Testcontainers); Swagger/Actuator are exposed; and it ships the full documentation set (architecture diagram, problem/solution, setup, API docs, README).

## Build toolchain

Target is **Java 21**. If your Maven launches under a newer JDK, pin `JAVA_HOME` to a JDK 21 (helper scripts under each project's `scripts/` do this automatically on macOS).
