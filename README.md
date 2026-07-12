# NovaPOS — Backend

Enterprise Point of Sale platform backend. **Phase 1 target: a modular monolith**, built with Spring Boot and PostgreSQL. UI comes later, once the backend is complete and tested — this repo is backend-only.

This README is the entry point. Read it, then read the other docs in this order:

1. `README.md` — this file. Stack, structure, how to run.
2. `ARCHITECTURE.md` — the modular monolith rules: how modules are organized, how they're allowed (and not allowed) to talk to each other.
3. `DATABASE.md` — PostgreSQL schema, per module, with migration conventions.
4. `API.md` — REST conventions, error format, auth.
5. `SKILLS.md` — how to work in this codebase: coding standards, testing bar, definition of done. Read this before writing any code.
6. `TASKS.md` — the actual step-by-step build plan, in order. Work through it top to bottom.

## Tech Stack

| Concern | Choice | Why |
|---|---|---|
| Language | Java 21 | LTS, virtual threads available for high-concurrency POS traffic |
| Framework | Spring Boot 3.3.x | Mature, first-class Postgres/JPA/security support |
| Build tool | Maven | Simpler dependency management for a single-repo modular monolith |
| Database | PostgreSQL 16 | Strong transactional guarantees, JSONB for flexible fields, mature ecosystem |
| Migrations | Flyway | Versioned, repeatable, plays well with CI |
| Persistence | Spring Data JPA + Hibernate | Standard, well understood; raw JDBC/`@Query` used where JPA would be awkward (reporting aggregates) |
| Security | Spring Security + JWT (jjwt) | Stateless auth, matches the multi-terminal / offline-sync architecture |
| Validation | Jakarta Bean Validation | Standard annotations on DTOs |
| API docs | springdoc-openapi | Auto-generates OpenAPI/Swagger from controllers |
| Testing | JUnit 5, Testcontainers (Postgres), ArchUnit | Real Postgres in tests, not H2 — behavior must match production; ArchUnit enforces module boundaries in CI |
| Module boundary enforcement | ArchUnit rules in `architecture-tests` | Prevents one module's code from reaching into another module's internals — this is what makes Phase 2 (microservices) extraction cheap later |

## Repository Structure

```
novapos-backend/
├── pom.xml
├── src/main/java/com/novapos/
│   ├── NovaPosApplication.java
│   ├── shared/              # cross-cutting: security, error handling, event bus config, common types
│   ├── company/              # Module: Company & Branch Management
│   ├── user/                 # Module: User & Access Management
│   ├── catalog/               # Module: Products, Categories, Brands
│   ├── inventory/             # Module: Inventory & Stock Movement
│   ├── pos/                  # Module: POS / Sales
│   ├── purchasing/            # Module: Purchasing & Suppliers
│   ├── customer/              # Module: Customers & CRM
│   ├── promotion/             # Module: Discount / Promotion / Loyalty Engine
│   ├── accounting/            # Module: Accounting
│   ├── restaurant/            # Module: Restaurant Operations (optional/toggle)
│   ├── reporting/             # Module: Reporting & Dashboards
│   └── notification/          # Module: Notifications
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/          # Flyway SQL migrations, prefixed per module (see DATABASE.md)
├── src/test/java/com/novapos/
│   ├── architecture/           # ArchUnit boundary tests
│   └── <module>/               # per-module unit + integration tests
└── docs/                       # this doc set
```

Each top-level package under `com.novapos` **is a module** in the sense defined in `ARCHITECTURE.md`. That mapping is not cosmetic — it's what an extraction to a microservice will follow later.

## Running Locally

Prerequisites: JDK 21, Maven 3.9+, Docker (for local Postgres via `docker-compose` or Testcontainers).

```bash
# start Postgres
docker compose up -d postgres

# run migrations + start the app
mvn spring-boot:run

# run the full test suite (spins up Testcontainers Postgres automatically)
mvn test
```

The app starts on `http://localhost:8080`. OpenAPI docs are served at `/swagger-ui.html` once springdoc is wired up (see `TASKS.md`, Task 1.4).

## What "Done" Looks Like for the Backend Phase

Before UI work starts, every module in `TASKS.md` must have: a working REST API, a Postgres schema with migrations, unit + integration test coverage, and ArchUnit boundary checks passing in CI. See `SKILLS.md` for the exact definition-of-done checklist applied to every task.
