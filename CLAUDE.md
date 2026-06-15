# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Current State

This repository is in the planning stage. No code exists yet — only the specification documents in `initial-docs/`, which are the source of truth for what gets built:

- `initial-docs/project-description.txt` — overall vision and scope
- `initial-docs/tech-stack.txt` — standardized technology choices
- `initial-docs/project-matrix.txt` — the 17 sections (00–16) and what each covers
- `initial-docs/repository-structure.txt` — the full planned directory tree

Read these before scaffolding or implementing anything; new projects must match the planned structure and stack.

## What This Repository Is

A reference implementation of enterprise application architecture, organized as many **independent, self-contained projects** grouped into numbered sections (`00-template-service` through `16-production-ready`). The shared business domain is a Supply Chain and Order Fulfillment Platform (Customer, Product, Inventory, Cart, Order, Payment, Shipment, Notification, Search, and Analytics services).

Key structural rules:

- Each project must be runnable on its own, with no dependency on other sections.
- Every project derives from the common baseline in `00-template-service` (Spring Boot + PostgreSQL + Flyway + Swagger + logging + testing + Docker).
- Each project must include: an architecture diagram, problem statement, solution description, setup instructions, API documentation, Docker support, automated tests, and a README explaining the architectural objective, business scenario, design decisions, implementation approach, and operational considerations.
- Sections progress from foundations (CRUD, layered/clean architecture) through databases, microservices, RabbitMQ, Kafka, Redis, NoSQL, search, resilience, observability, security, Docker, Kubernetes, AWS, advanced patterns (Saga, CQRS, event sourcing, DDD, multi-tenancy), and finally a production-ready combined platform.

## Standardized Tech Stack

All projects use the same stack for consistency:

- **Language/Framework:** Java 21, Spring Boot 3.x, Maven
- **Persistence:** PostgreSQL (default), Spring Data JPA/Hibernate, Flyway migrations; MongoDB, Cassandra, DynamoDB for the NoSQL section
- **Messaging:** RabbitMQ (queue/pub-sub patterns), Apache Kafka (event-driven/streaming)
- **Caching/Search:** Redis; OpenSearch
- **Resilience/Observability:** Resilience4j; Micrometer, Prometheus, Grafana, OpenTelemetry tracing, ELK/OpenSearch logging
- **Testing:** JUnit 5, Mockito, Testcontainers, WireMock
- **API docs:** OpenAPI/Swagger
- **Infra:** Docker + Docker Compose, Kubernetes + Helm, AWS (ECS, EKS, RDS, ElastiCache, SQS, SNS, MSK, CloudWatch, X-Ray, S3, Lambda), GitHub Actions for CI/CD

## Project Structure

- `src/main/java/com/[app]/controller/` — REST controllers (thin layer, no business logic)
- `src/main/java/com/[app]/service/` — business logic
- `src/main/java/com/[app]/repository/` — JPA repositories, all DB access goes here
- `src/main/java/com/[app]/domain/` — JPA entities
- `src/main/java/com/[app]/dto/` — request/response DTOs (never expose entities directly)
- `src/main/java/com/[app]/messaging/` — RabbitMQ producers and consumers
- `src/main/java/com/[app]/config/` — Spring config classes (Redis, RabbitMQ, Security, AWS)
- `src/main/java/com/[app]/exception/` — global exception handler, custom exceptions
- `src/test/java/` — mirrors main structure; unit tests alongside service classes

## Code Style Rules

- Use constructor injection, never field injection (`@Autowired` on fields is banned)
- Controllers must be thin — no business logic, delegate to service layer
- Never expose JPA entities in API responses — always map to DTOs
- Use `@Transactional` at service layer only, never at controller layer
- All exceptions must be handled by the global `@ControllerAdvice` handler
- Use `Optional` return types from repositories — never return null
- Lomboked entities are fine; use `@Data` only on DTOs, not JPA entities (causes hashCode issues)
- RabbitMQ producers go in `messaging/producers/`; consumers in `messaging/consumers/`
- Redis access only through a dedicated cache service class — no direct `RedisTemplate` calls in business logic

## Commands

Once projects exist, each is a standalone Maven project (run commands from the individual project directory):

```bash
mvn clean verify                 # build + tests
mvn test -Dtest=ClassName        # single test class
mvn test -Dtest=ClassName#method # single test method
mvn spring-boot:run              # run the service
docker compose up -d             # start project infrastructure (DB, brokers, etc.)
```

Integration tests use Testcontainers, so Docker must be running.
