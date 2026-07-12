# NovaPOS Backend — Architecture

This document defines how the modular monolith is organized in Spring Boot, and the rules that keep it extractable into microservices later without a rewrite. Every task in `TASKS.md` must follow this.

## 1. Core Principle

**Package by module, not by layer.** The top-level split is `com.novapos.pos`, `com.novapos.inventory`, `com.novapos.accounting`, etc. — not `com.novapos.controller` / `com.novapos.service` / `com.novapos.repository`. Inside each module package, layer as needed (`pos.api`, `pos.service`, `pos.domain`, `pos.repository`).

Why: a module package is the unit that gets extracted into its own service in Phase 2. If layers are the top-level split instead, there is no clean seam to cut along later.

## 2. Module Anatomy

Every module package follows this internal shape:

```
com.novapos.<module>/
├── api/                 # PUBLIC surface other modules are allowed to call
│   ├── <Module>Facade.java      # interface — the only entry point other modules use
│   └── dto/                     # DTOs used across the public boundary (immutable records)
├── web/                  # REST controllers (external HTTP API), request/response DTOs
├── domain/                # JPA entities, domain logic, enums
├── repository/             # Spring Data repositories
├── service/                # internal service classes implementing the Facade
├── event/                  # domain events this module publishes/subscribes to
└── config/                 # module-specific Spring configuration, if needed
```

## 3. The Rule That Matters Most: No Cross-Module Internals

- A module may **only** depend on another module's `api` package (the `Facade` interface and its DTOs). It may never import another module's `domain`, `repository`, `service`, or `web` classes.
- A module may **never** query another module's database tables directly (no JPA entity from module A mapped against module B's table, no native SQL joining across module schemas).
- This is enforced automatically — see Section 6, ArchUnit rules. A pull request that violates this fails CI, not just code review.

## 4. How Modules Talk to Each Other

Two mechanisms only:

**a) Synchronous facade calls**, for "I need an answer right now to keep doing my own work" — e.g., POS calling `InventoryFacade.reserveStock(...)` during checkout. This is a normal Java method call today; it becomes a network call (REST or gRPC) if that module is ever extracted, with the Facade interface unchanged.

**b) Domain events**, for "something happened, and other modules may want to react, but I don't need to wait for them" — e.g., POS publishing `SaleCompletedEvent` after a sale finishes; Inventory, Accounting, and Notification modules each have a listener that reacts independently. Use Spring's `ApplicationEventPublisher` for Phase 1; the event names and payloads are designed to map 1:1 onto the message-broker events described in the companion technical design doc, so switching the transport later doesn't change the event contract.

**Rule of thumb**: if the calling module cannot proceed without the answer, use a Facade call. If it's a "by the way, this happened," publish an event.

## 5. Data Ownership

- Each module owns its own set of database tables. Table names are prefixed by module where useful for clarity (see `DATABASE.md`), and every table has exactly one owning module in code.
- No shared mutable tables. If two modules seem to need the same data, one module owns it and exposes it via its Facade; the other module does not get its own copy or its own write access.
- Cross-module "joins" for reporting purposes happen in the `reporting` module, which is allowed to read (never write) via Facade calls or dedicated read-projections — it does not get a free pass to bypass the rule in Section 3.

## 6. Enforcing This Automatically (ArchUnit)

`src/test/java/com/novapos/architecture/ModuleBoundaryTest.java` must contain rules equivalent to:

- No class in `com.novapos.(module).domain` is accessed from outside `com.novapos.(module)..`.
- No class in `com.novapos.(module).repository` is accessed from outside `com.novapos.(module)..`.
- No class in `com.novapos.(module).service` is accessed from outside `com.novapos.(module)..`, except by that module's own `api` package.
- Only `com.novapos.(module).api..` classes may be imported by other `com.novapos.*` modules.

This test suite runs in CI on every build. A module boundary violation is a build failure, not a warning — see `TASKS.md` Task 1.3 for when this gets set up (early — before the first real module is built).

## 7. Transactions

- A single `@Transactional` boundary never spans two modules' repositories. If a business action (e.g., "complete a sale") needs effects in POS, Inventory, and Accounting, POS completes its own transaction, then publishes `SaleCompletedEvent`; Inventory and Accounting handle that event in their own, separate transactions.
- This means a business action is **eventually consistent** across modules, not atomically consistent across all of them in one database transaction. Each module's event listener must be idempotent (safe to process the same event twice) since Spring's in-process event bus does not guarantee exactly-once delivery. Track processed event IDs in a small `processed_event` table per module if a listener has side effects that must not double-apply (e.g., don't decrement inventory twice for the same sale).

## 8. Why Modular Monolith Before Microservices

Documented in full in the PRD; summarized here for engineering context: enforcing Sections 3–7 from day one means that extracting any single module (`pos`, `inventory`, etc.) into its own Spring Boot service later is a mechanical exercise — stand up the Facade as a REST controller, point the calling module's Facade implementation at HTTP instead of a local bean, move that module's tables to their own database. The public contract (Facade interface + DTOs + event shapes) does not change shape when that happens.

## 9. Configuration & Environments

- One `application.yml` with Spring profiles: `local`, `test`, `staging`, `prod`. Module-specific config (e.g., a feature flag toggling the `restaurant` module on/off per deployment) lives under a namespaced key, e.g. `novapos.modules.restaurant.enabled`.
- Secrets (DB credentials, JWT signing key) are never committed — sourced from environment variables, documented (names only, not values) in `README.md`'s environment section once Task 1.1 sets that up.
