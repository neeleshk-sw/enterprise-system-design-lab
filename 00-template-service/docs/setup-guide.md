# Setup Guide — 00-template-service

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | **21** | Build target. `java` and the JDK `mvn` uses must be 21 (see Toolchain note) |
| Maven | 3.9+ | Or use the helper scripts |
| Docker | recent | Required for the full stack and for integration tests (Testcontainers) |

## Toolchain note (important)

The project targets **Java 21**. If your `mvn` launches under a newer JDK, pin `JAVA_HOME` for the build:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"   # macOS
mvn clean verify
```

The helper scripts (`scripts/build.sh`, `scripts/run-local.sh`) do this automatically on macOS when `JAVA_HOME` is unset.

## Build & test

```bash
scripts/build.sh            # mvn clean verify — compiles, runs unit + integration tests
# or directly:
mvn clean verify
```

- Unit tests (`*Test`) run under Surefire — fast, no Docker.
- Integration tests (`*IT`) run under Failsafe — start Testcontainers PostgreSQL (**Docker must be running**).

Run a single test:

```bash
mvn -pl customer-service test -Dtest=CustomerServiceImplTest
mvn -pl customer-service verify -Dit.test=CustomerControllerIT
```

## Run locally (app on host, Postgres in Docker)

```bash
scripts/run-local.sh
# equivalent to:
docker compose up -d postgres
mvn -pl customer-service -am spring-boot:run
```

Service starts on `http://localhost:8080`.

## Run the full stack (everything in Docker)

```bash
scripts/docker-up.sh        # docker compose up --build
# in another shell, smoke test:
curl -X POST http://localhost:8080/api/v1/customers \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com"}'
curl http://localhost:8080/api/v1/customers/1

scripts/docker-down.sh -v   # stop and remove the DB volume
```

The app container waits for Postgres to be healthy before starting, then reports healthy once `/actuator/health` is `UP`.

## Configuration

Datasource and port are env-driven (12-factor). Defaults target localhost; the `docker` profile targets the compose `postgres` service.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/customer` | JDBC URL |
| `DB_USERNAME` | `customer` | DB user |
| `DB_PASSWORD` | `customer` | DB password |
| `SERVER_PORT` | `8080` | HTTP port |
| `SPRING_PROFILES_ACTIVE` | (none) | set to `docker` inside compose |
| `JAVA_OPTS` | (empty) | extra JVM flags for the container |

Profiles:

- **base** (`application.yml`) — localhost defaults for local runs.
- **docker** (`application-docker.yml`) — datasource points at the `postgres` compose service.
- **tests** — datasource provided dynamically by Testcontainers (`@ServiceConnection`); no profile file needed.

## Verify it works

| Check | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Health | `http://localhost:8080/actuator/health` |
| Metrics | `http://localhost:8080/actuator/metrics` |

## Troubleshooting

- **`invalid target release: 21` / compiled with wrong Java** — `mvn` is using a non-21 JDK. Set `JAVA_HOME` (see Toolchain note).
- **Integration tests fail to start containers** — ensure Docker is running (`docker info`).
- **Flyway/schema validation error on startup** — the DB schema drifted from the entities; add a new `V_n__*.sql` migration rather than changing `ddl-auto`.
- **Port 8080/5432 already in use** — stop the conflicting process or change `SERVER_PORT` / the compose port mapping.

## Start a new project from this template

1. Copy the `00-template-service` tree to `NN-section/<project>/`.
2. Find/replace `com.enterprise.customer` → `com.enterprise.<project>`; rename artifact ids and the application class.
3. Replace the Customer domain (entity, DTOs, mapper, repository, service, controller) and `V1__*.sql`.
4. Update `docker-compose.yml`, `application*.yml`, and the docs.
5. Keep `common-library` unchanged.
