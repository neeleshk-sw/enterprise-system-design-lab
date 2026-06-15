# Enterprise System Design Lab — Master Plan

This document is the single reference point for building the lab. Read it before starting any individual project. It consolidates the four specification documents in `initial-docs/` (project description, tech stack, project matrix, repository structure) into one actionable plan.

---

## 1. Vision & Goals

**What this is:** a comprehensive reference implementation of modern enterprise application architecture, organized as many **independent, self-contained projects** grouped into 17 numbered sections (`00`–`16`). It serves three purposes:

- a long-term **architect reference platform**,
- an **interview preparation** resource,
- a hands-on **enterprise technology laboratory**.

**What this is not:** a single evolving application. Each project demonstrates one architectural concept, integration pattern, scalability technique, or cloud-native practice in isolation. Projects never depend on each other.

---

## 2. Shared Business Domain

All projects draw from one domain — a **Supply Chain and Order Fulfillment Platform** — so patterns are demonstrated against realistic, consistent scenarios.

| Service | Responsibility | Core entities |
|---|---|---|
| Customer Service | Customer registration, profiles, addresses | Customer, Address |
| Product Service | Product catalog, categories, pricing | Product, Category, Price |
| Inventory Service | Stock levels, reservations, warehouse locations | InventoryItem, Reservation, Warehouse |
| Cart Service | Shopping cart lifecycle, cart items | Cart, CartItem |
| Order Service | Order placement, state transitions, history | Order, OrderItem, OrderStatus |
| Payment Service | Payment authorization, capture, refunds | Payment, Transaction, Refund |
| Shipment Service | Shipment creation, tracking, delivery status | Shipment, TrackingEvent |
| Notification Service | Email/SMS/push notifications on domain events | Notification, NotificationTemplate |
| Search Service | Product and order search, autocomplete, facets | SearchIndex, SearchQuery |
| Analytics Service | Aggregations, reports, time-series metrics | MetricEvent, Report |

Each project borrows whichever service(s) make its pattern realistic — e.g., Inventory for Redis caching and locking, Product for MongoDB and OpenSearch, Order + Payment + Shipment for Sagas, Analytics for Kafka Streams and Cassandra time-series. The Analytics Service has no dedicated section; it is exercised through Kafka Streams (05), Cassandra time-series (07), and dashboards (10).

---

## 3. Standardized Tech Stack

One stack across all projects, for consistency and comparability:

- **Language/Framework:** Java 21, Spring Boot 3.x, Maven
- **Relational persistence:** PostgreSQL (default), Spring Data JPA / Hibernate, Flyway migrations
- **NoSQL (section 07):** MongoDB (documents), Cassandra (event/time-series), DynamoDB (cloud-native)
- **Messaging:** RabbitMQ (queues, exchanges, pub/sub), Apache Kafka (event-driven, streaming)
- **Caching/coordination:** Redis
- **Search:** OpenSearch
- **Resilience:** Resilience4j
- **Observability:** Micrometer, Prometheus, Grafana, OpenTelemetry tracing, ELK/OpenSearch logging
- **Testing:** JUnit 5, Mockito, Testcontainers, WireMock
- **API documentation:** OpenAPI / Swagger
- **Containers:** Docker, Docker Compose
- **Orchestration:** Kubernetes, Helm
- **AWS (section 14):** ECS, EKS, RDS, ElastiCache, SQS, SNS, MSK, CloudWatch, X-Ray, S3, IAM, Secrets Manager, Lambda
- **CI/CD:** GitHub Actions

**Engineering principles:** clean coding, Domain-Driven Design where applicable, RESTful API standards, Twelve-Factor App principles, secure-by-default practices, infrastructure-as-code concepts, production-grade operational patterns.

---

## 4. Repository-Wide Conventions

### 4.1 Hard rules

1. **Independence.** Every project runs standalone: own Maven build, own `docker-compose.yml` for its infrastructure, no dependency on any other section or project.
2. **Common baseline.** Every project derives from `00-template-service` (Spring Boot + PostgreSQL + Flyway + Swagger + logging + testing + Docker). Start a project by copying the template, never from scratch.
3. **Documentation contract.** Every project ships all nine artifacts: architecture diagram, problem statement, solution description, setup instructions, API documentation, infrastructure definitions, Docker support, automated tests, and a detailed README.

### 4.2 Naming and layout

- Project directories: `NN-section-name/project-name/` (kebab-case, as already scaffolded).
- Java packages: `com.esdl.<section>.<project>` (e.g., `com.esdl.redis.cacheaside`), service modules keep domain names (e.g., `com.esdl.template.customer`).
- Standard Maven/Spring layout inside each project: `src/main/java`, `src/main/resources/db/migration` (Flyway), `src/test/java`, `docker-compose.yml`, `Dockerfile`, `README.md`, `docs/` (diagram + notes) as needed.

### 4.3 Port allocation

To allow running any single project without conflicts, use consistent host ports: application `8080` (additional services `8081+`), PostgreSQL `5432`, RabbitMQ `5672`/`15672`, Kafka `9092`, Redis `6379`, MongoDB `27017`, Cassandra `9042`, OpenSearch `9200`, Prometheus `9090`, Grafana `3000`. Projects are run one at a time; `docker compose down` before switching.

### 4.4 README template (the documentation contract)

Every project README follows this skeleton:

```markdown
# <Project Name>

## Architectural Objective       — the concept this project demonstrates
## Business Scenario             — which domain service(s) and why
## Problem Statement             — the concrete problem being solved
## Solution & Design Decisions   — approach, trade-offs, alternatives rejected
## Architecture Diagram          — image or Mermaid block
## Implementation Approach       — key classes/flows, what to read first
## Setup & Run                   — docker compose up -d, mvn spring-boot:run
## API Documentation             — Swagger UI URL + key endpoints
## Testing                       — how to run, what is covered
## Operational Considerations    — failure modes, scaling, monitoring notes
```

### 4.5 Definition of Done (per project)

- [ ] Derived from `00-template-service`; builds with `mvn clean verify`
- [ ] Runs end-to-end with `docker compose up -d` + `mvn spring-boot:run`
- [ ] Flyway migrations for all schema (where a relational DB is used)
- [ ] Swagger UI exposes and documents the API
- [ ] Unit tests + integration tests (Testcontainers) passing
- [ ] Architecture diagram present
- [ ] README complete per the template above
- [ ] Demonstrated pattern is observable (logs, endpoints, dashboards, or tests prove it works)

---

## 5. Section-by-Section Project Plan

### 00-template-service — Standard starter for all examples

The baseline everything derives from. **Build this first and most carefully** — its conventions propagate to every other project.

| Project | Content |
|---|---|
| `customer-service` | Reference Spring Boot service: Customer CRUD, layered packages, PostgreSQL + Flyway, Swagger, structured logging, unit + Testcontainers tests, Dockerfile |
| `common-library` | Shared starter utilities: error model/exception handling, API response conventions, logging config, base test support |
| `docker` | Compose baseline (PostgreSQL + service), image conventions |

### 01-foundation — Core application architecture

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `basic-crud` | Plain CRUD REST service done right | Customer | PostgreSQL |
| `layered-architecture` | Controller/Service/Repository layering, DTO mapping | Product | PostgreSQL |
| `clean-architecture` | Entities/use-cases/adapters, dependency rule | Order | PostgreSQL |
| `multi-module` | Maven multi-module split (api/domain/persistence/app) | Customer | PostgreSQL |
| `rest-api-best-practices` | Versioning, pagination, filtering, error format, HATEOAS-lite, idempotency keys | Product | PostgreSQL |

### 02-database-patterns — Relational database mastery

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `postgres-indexing` | B-tree/GIN/partial indexes, EXPLAIN-driven optimization | Product search queries | PostgreSQL |
| `postgres-partitioning` | Range/list partitioning of large tables | Order history | PostgreSQL |
| `transaction-management` | Propagation, isolation levels, anomalies demonstrated | Order + Payment | PostgreSQL |
| `optimistic-locking` | @Version, conflict handling, retry on stale state | Inventory stock updates | PostgreSQL |
| `pessimistic-locking` | SELECT FOR UPDATE, lock scope and deadlocks | Inventory reservation | PostgreSQL |
| `read-replica-pattern` | Read/write routing, replication lag handling | Product catalog reads | PostgreSQL primary + replica |
| `database-migration-flyway` | Versioned/repeatable migrations, evolution strategies, rollback approaches | Customer schema evolution | PostgreSQL |

### 03-microservices — Service decomposition

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `service-splitting` | Decomposing a monolith into service boundaries | Order + Inventory + Customer | PostgreSQL ×N |
| `synchronous-communication` | REST inter-service calls, timeouts, error propagation | Order → Inventory | PostgreSQL |
| `config-server` | Spring Cloud Config centralized configuration | any | Config server |
| `service-discovery` | Eureka registration and client-side discovery | Order ↔ Inventory | Eureka |
| `api-gateway` | Spring Cloud Gateway routing, filters, cross-cutting concerns | all | Gateway |
| `openfeign` | Declarative HTTP clients, fallbacks | Order → Payment | PostgreSQL |
| `distributed-configuration` | Profiles, refresh scope, environment-specific config | any | Config server + bus |

### 04-rabbitmq — Message queue and pub/sub patterns

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `basic-queue` | Producer/consumer over a single queue | Order events | RabbitMQ |
| `work-queue` | Competing consumers, prefetch, acknowledgements | Notification dispatch | RabbitMQ |
| `direct-exchange` | Routing-key based delivery | Payment status routing | RabbitMQ |
| `topic-exchange` | Wildcard routing patterns | Shipment event routing | RabbitMQ |
| `fanout-pubsub` | Broadcast to all bound queues | Order placed → many consumers | RabbitMQ |
| `dead-letter-queue` | DLX, rejection, TTL-expiry handling, poison messages | Payment failures | RabbitMQ |
| `delayed-processing` | Delayed delivery (TTL + DLX / delay plugin) | Cart abandonment reminders | RabbitMQ |
| `request-reply-pattern` | RPC over messaging, correlation IDs, reply queues | Inventory availability check | RabbitMQ |

### 05-kafka — Event-driven architecture

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `producer-consumer` | Basic produce/consume, serialization, offsets | Order events | Kafka |
| `consumer-groups` | Scaling consumption, rebalancing | Notification consumers | Kafka |
| `partitioning` | Key-based partitioning, ordering guarantees | Orders by customer | Kafka |
| `retry-topics` | Tiered retry topics with backoff | Payment processing | Kafka |
| `dead-letter-topic` | DLT routing and reprocessing | Shipment events | Kafka |
| `idempotent-consumers` | De-duplication, exactly-once-effect processing | Payment events | Kafka + PostgreSQL |
| `outbox-pattern` | Transactional outbox + relay (DB → Kafka) | Order placement | Kafka + PostgreSQL |
| `kafka-streams` | Stateful stream processing, aggregations, windows | Analytics on order stream | Kafka |
| `event-sourcing` | Events as source of truth, replay, projections | Order lifecycle | Kafka + PostgreSQL |
| `transactional-messaging` | Kafka transactions, read-process-write atomicity | Payment + ledger | Kafka |

### 06-redis — Caching and performance

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `cache-aside` | Lazy caching, TTLs, invalidation | Product lookups | Redis + PostgreSQL |
| `write-through` | Synchronous cache+DB writes | Product pricing | Redis + PostgreSQL |
| `write-behind` | Async write-back, batching, loss trade-offs | Cart updates | Redis + PostgreSQL |
| `distributed-lock` | SETNX/Redisson locks, lease/renewal, fencing | Inventory deduction | Redis |
| `session-management` | Spring Session on Redis | Customer sessions | Redis |
| `rate-limiting` | Token bucket / sliding window in Redis | API protection | Redis |
| `inventory-cache` | Hot stock counters, atomic ops, consistency with DB | Inventory | Redis + PostgreSQL |
| `pubsub-events` | Redis Pub/Sub vs. broker trade-offs | Price-change notifications | Redis |

### 07-nosql — Polyglot persistence

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `mongodb-product-catalog` | Flexible document schemas, embedded vs. referenced | Product catalog | MongoDB |
| `mongodb-reviews` | Nested documents, aggregation pipeline | Product reviews | MongoDB |
| `cassandra-event-store` | Wide-row event storage, partition key design | Order events | Cassandra |
| `cassandra-time-series` | Time-bucketed series, TTL, compaction | Analytics metrics | Cassandra |
| `dynamodb-order-store` | Single-table design, GSIs, cloud-native NoSQL | Orders | DynamoDB (local) |
| `polyglot-persistence` | Right store per concern in one system | Product + Order + Analytics | PostgreSQL + MongoDB + Cassandra |
| `schema-design-comparison` | Same domain modeled across stores, trade-off analysis | Order | PostgreSQL + MongoDB + Cassandra + DynamoDB |

### 08-search — Search architecture

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `opensearch-basics` | Index/mapping/analyzers, CRUD queries | Product | OpenSearch |
| `indexing-pipeline` | DB → index synchronization strategies | Product | OpenSearch + PostgreSQL |
| `product-search` | Full-text search, filters, pagination | Product | OpenSearch |
| `autocomplete` | Edge n-grams / completion suggesters | Product names | OpenSearch |
| `faceted-search` | Aggregation-based facets | Product categories/brands | OpenSearch |
| `search-ranking` | Relevance tuning, boosting, function scores | Product | OpenSearch |
| `search-with-kafka` | Event-driven near-real-time indexing | Product updates | OpenSearch + Kafka |

### 09-resilience — Fault tolerance (Resilience4j throughout)

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `retry-pattern` | Retry with backoff/jitter, retryable vs. not | Order → Payment | WireMock downstream |
| `circuit-breaker` | States, thresholds, half-open probes | Order → Inventory | WireMock downstream |
| `bulkhead` | Thread-pool/semaphore isolation | Mixed downstream calls | WireMock |
| `timeout-management` | TimeLimiter, connection vs. read timeouts | Shipment tracking | WireMock |
| `fallback-pattern` | Graceful degradation, cached/default responses | Product recommendations | WireMock + Redis |
| `rate-limiter` | Resilience4j RateLimiter (client-side) | Notification sending | — |
| `resilience-combination` | Composing retry+CB+bulkhead+timeout correctly | Order checkout flow | WireMock |

### 10-observability — Monitoring and diagnostics

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `centralized-logging` | Structured JSON logs, correlation IDs, log aggregation | Order flow | OpenSearch/ELK |
| `distributed-tracing` | OpenTelemetry traces across services | Order → Payment → Shipment | OTel collector + Jaeger/Tempo |
| `metrics-collection` | Micrometer counters/timers/gauges, custom metrics | Order throughput | Prometheus |
| `prometheus` | Scraping, PromQL, recording rules | service metrics | Prometheus |
| `grafana` | Dashboards as code, RED/USE views | platform metrics | Grafana + Prometheus |
| `alerting` | Alert rules, Alertmanager routing | SLO breaches | Prometheus + Alertmanager |
| `complete-observability-stack` | Logs + metrics + traces integrated | Order flow | full stack via Compose |

### 11-security — Enterprise security

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `jwt-authentication` | Stateless JWT issuance/validation, refresh tokens | Customer auth | PostgreSQL |
| `oauth2` | Authorization code flow, resource/authorization servers | Customer auth | Spring Authorization Server |
| `keycloak-integration` | External IdP, realms, token validation | Customer auth | Keycloak |
| `role-based-access` | Roles/authorities, method security | Admin vs. customer on Orders | PostgreSQL |
| `api-security` | CORS, headers, input validation, OWASP basics | any API | — |
| `secret-management` | Externalized secrets, Vault/env strategies | DB/broker credentials | Vault or env |
| `security-hardening` | TLS, least privilege, actuator lockdown, dependency scanning | template service | — |

### 12-docker — Containerization

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `service-containerization` | Dockerfile for a Spring Boot service, JVM tuning in containers | Customer | Docker |
| `multi-stage-build` | Build vs. runtime stages, layer caching | Customer | Docker |
| `docker-compose` | Multi-container app + infra orchestration | Order + Postgres + Rabbit | Compose |
| `networking` | Bridge networks, service DNS, isolation | two services | Compose |
| `volumes` | Data persistence, bind mounts vs. volumes | PostgreSQL data | Compose |
| `production-images` | Distroless/JRE-slim, non-root, health checks, image scanning | Customer | Docker |

### 13-kubernetes — Container orchestration

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `deployments` | Deployments, replicas, rolling updates, probes | Customer | k8s (kind/minikube) |
| `services` | ClusterIP/NodePort/LoadBalancer | Customer | k8s |
| `ingress` | Ingress controller, path routing, TLS | Gateway + services | k8s |
| `configmaps` | Externalized config, mounted vs. env | Customer | k8s |
| `secrets` | Secret management, mounting, sealed-secret concepts | DB credentials | k8s |
| `autoscaling` | HPA on CPU/custom metrics | Order under load | k8s + metrics-server |
| `statefulsets` | Stateful workloads, PVCs, headless services | PostgreSQL | k8s |
| `helm` | Charts, values, templating, releases | Customer service chart | Helm |

### 14-cloud-native-aws — AWS-native implementation

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `ecs-deployment` | Fargate task/service deployment | Customer | ECS |
| `eks-deployment` | EKS cluster deployment, IRSA | Customer | EKS |
| `rds-integration` | RDS PostgreSQL, Secrets Manager credentials | Customer | RDS |
| `elasticache` | ElastiCache Redis from Spring | Product cache | ElastiCache |
| `sqs-sns` | SQS queues, SNS fan-out | Notification | SQS + SNS (LocalStack locally) |
| `msk-kafka` | Managed Kafka integration | Order events | MSK |
| `cloudwatch` | Logs, metrics, dashboards, alarms | service ops | CloudWatch |
| `xray-tracing` | X-Ray tracing integration | Order flow | X-Ray |
| `s3-document-storage` | S3 upload/download, presigned URLs | invoices/documents | S3 (LocalStack locally) |
| `serverless-integration` | Lambda triggered by SQS/S3 events | Notification | Lambda (LocalStack locally) |

> Local-first rule: where possible, AWS projects must also run locally via LocalStack so the independence rule holds without an AWS account; real-AWS deployment steps documented per project.

### 15-architecture-patterns — Advanced enterprise patterns

| Project | Demonstrates | Domain | Infra |
|---|---|---|---|
| `saga-orchestration` | Central orchestrator, compensation logic | Order → Payment → Inventory → Shipment | Kafka or RabbitMQ + PostgreSQL |
| `saga-choreography` | Event-chained saga, no central coordinator | same flow | Kafka + PostgreSQL |
| `cqrs` | Separate command/query models, projections | Order write / Order-view read | Kafka + PostgreSQL |
| `event-sourcing` | Aggregate state from events, snapshots, replay (architectural treatment) | Order | PostgreSQL/Kafka event store |
| `outbox-pattern` | Outbox as architectural consistency boundary (vs. 05 mechanics) | Order + Notification | Kafka + PostgreSQL |
| `strangler-pattern` | Incremental legacy replacement behind a facade | legacy Order monolith → services | Gateway + PostgreSQL |
| `bff-pattern` | Per-client backends (web/mobile) | storefront views | two BFFs + services |
| `multi-tenancy` | Tenant isolation: shared schema / schema-per-tenant / DB-per-tenant | tenant-scoped Orders | PostgreSQL |
| `hexagonal-architecture` | Ports & adapters, swappable infrastructure | Inventory | PostgreSQL + Kafka |
| `domain-driven-design` | Bounded contexts, aggregates, domain events, ubiquitous language | Order fulfillment context | PostgreSQL |

### 16-production-ready — Real-world deployment readiness

| Project | Demonstrates |
|---|---|
| `complete-platform` | Capstone: multiple domain services combined with messaging, caching, search, observability, security — the lab's concepts integrated |
| `performance-testing` | Load/stress testing (Gatling/k6), baselines, bottleneck analysis |
| `chaos-engineering` | Failure injection experiments, steady-state hypotheses, blast radius |
| `deployment-pipeline` | GitHub Actions CI/CD: build, test, scan, image publish, deploy |
| `disaster-recovery` | Backup/restore, RTO/RPO strategies, failover drills |
| `scaling-strategies` | Horizontal/vertical scaling, capacity planning, scalability validation |
| `production-checklist` | Operational readiness checklist, runbooks, security hardening review |

---

## 6. Build Order & Phases

Build in dependency-of-knowledge order; within a phase, projects are independent and can be built in any order.

| Phase | Sections | Rationale |
|---|---|---|
| **0** | `00-template-service` | Critical path — every later project copies it. Get package layout, common-library scope, Docker, testing, and the README template right here. |
| **1** | `01-foundation`, `02-database-patterns` | Core service and persistence skills the rest assume. |
| **2** | `03-microservices`, `04-rabbitmq`, `05-kafka` | Decomposition and inter-service communication. |
| **3** | `06-redis`, `07-nosql`, `08-search` | Data-layer specialization. |
| **4** | `09-resilience`, `10-observability`, `11-security` | Production cross-cutting concerns. |
| **5** | `12-docker`, `13-kubernetes`, `14-cloud-native-aws` | Packaging, orchestration, cloud. |
| **6** | `15-architecture-patterns`, `16-production-ready` | Advanced patterns, then the capstone that combines everything. |

**Deliberate overlaps (by design, different angles — do not deduplicate):**
- `outbox-pattern` and `event-sourcing`: section 05 covers Kafka mechanics; section 15 covers the architectural treatment.
- Rate limiting: `06-redis/rate-limiting` is the server-side Redis implementation; `09-resilience/rate-limiter` is the client-side Resilience4j pattern.
- Flyway: baseline usage in `00`; migration strategy patterns in `02-database-patterns/database-migration-flyway`.
- Docker: every project ships basic Docker support; section 12 studies containerization itself.

---

## 7. How to Start a New Project

1. **Copy** `00-template-service/customer-service` into the target section directory and rename (artifact ID, package `com.esdl.<section>.<project>`, application name).
2. **Write the README first** from the template in §4.4 — objective, scenario, problem statement. This forces the design before the code.
3. **Define infrastructure** in the project's `docker-compose.yml` (only what this project needs).
4. **Implement** the pattern with the standardized stack; keep domain code realistic but minimal — the pattern is the point, not the domain.
5. **Add Flyway migrations** for any relational schema.
6. **Test**: unit tests (JUnit 5 + Mockito) and integration tests (Testcontainers; WireMock for external HTTP).
7. **Diagram** the architecture (Mermaid in README or image in `docs/`).
8. **Verify the Definition of Done** checklist (§4.5) before considering the project complete.
