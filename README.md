# Enterprise System Design Lab

[![CI](https://github.com/neeleshk-sw/enterprise-system-design-lab/actions/workflows/ci.yml/badge.svg)](https://github.com/neeleshk-sw/enterprise-system-design-lab/actions/workflows/ci.yml)

A comprehensive, hands-on reference implementation of modern enterprise application architecture — organized as many **independent, self-contained projects** grouped into 17 numbered sections (`00` → `16`). It is intended as a long-term architect reference, an interview-preparation resource, and a practical technology laboratory.

The shared business domain across all projects is a **Supply Chain & Order Fulfillment Platform** (Customer, Product, Inventory, Cart, Order, Payment, Shipment, Notification, Search, Analytics). Each project borrows whichever services make its pattern realistic — projects never depend on each other.

> 📖 The full build plan lives in **[docs/plan/plan.md](docs/plan/plan.md)** — vision, conventions, the per-section project catalogue, and the recommended build order.

## Status

| | Section | Status |
|---|---|---|
| ✅ | `00-template-service` | **Complete** — the baseline every other project is copied from |
| 🚧 | `01-foundation` … `16-production-ready` | Scaffolded; built incrementally |

## Sections

| # | Section | Focus |
|---|---------|-------|
| 00 | template-service | Baseline: clean layered Spring Boot service |
| 01 | foundation | CRUD, layered & clean architecture, REST standards |
| 02 | database-patterns | Indexing, partitioning, transactions, locking, migrations |
| 03 | microservices | Decomposition, config server, discovery, gateway, Feign |
| 04 | rabbitmq | Queues, exchanges, routing, DLQ, pub/sub |
| 05 | kafka | Producer/consumer, streams, outbox, event sourcing |
| 06 | redis | Cache strategies, distributed locks, rate limiting |
| 07 | nosql | MongoDB, Cassandra, DynamoDB, polyglot persistence |
| 08 | search | OpenSearch indexing, full-text, autocomplete, ranking |
| 09 | resilience | Retry, circuit breaker, bulkhead, timeout, fallback |
| 10 | observability | Logging, metrics, tracing, dashboards, alerting |
| 11 | security | JWT, OAuth2, RBAC, secret management |
| 12 | docker | Dockerfiles, Compose, networking, image optimization |
| 13 | kubernetes | Deployments, services, ingress, HPA, Helm |
| 14 | cloud-native-aws | ECS, EKS, RDS, ElastiCache, SQS/SNS, MSK, CloudWatch |
| 15 | architecture-patterns | Saga, CQRS, event sourcing, DDD, multi-tenancy |
| 16 | production-ready | CI/CD, DR, scaling, performance, chaos engineering |

## Standardized Tech Stack

Java 21 · Spring Boot 3.x · Maven · PostgreSQL + JPA/Hibernate + Flyway · RabbitMQ · Kafka · Redis · MongoDB/Cassandra/DynamoDB · OpenSearch · Resilience4j · Micrometer/Prometheus/Grafana/OpenTelemetry · JUnit 5 · Mockito · Testcontainers · WireMock · OpenAPI/Swagger · Docker/Compose · Kubernetes/Helm · AWS · GitHub Actions.

## Getting Started

Each project is standalone. Start with the baseline:

```bash
cd 00-template-service
scripts/docker-up.sh        # full stack (Postgres + service) on :8080
```

See **[00-template-service/README.md](00-template-service/README.md)** for the reference implementation and **[CONTRIBUTING.md](CONTRIBUTING.md)** for how new projects are structured.

## Conventions

Every project: derives from `00-template-service`; is independently runnable; and ships an architecture diagram, problem statement, solution description, setup instructions, API docs, Docker support, automated tests, and a detailed README. Source specifications are in **[initial-docs/](initial-docs/)**.
