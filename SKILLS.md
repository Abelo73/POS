# NovaPOS Backend — Working Rules ("Skills")

This file tells you (the AI, or any engineer) **how** to work in this codebase, not what to build — `TASKS.md` is the what, in order. Read this fully before starting Task 1.1, and re-check it before every task.

## 1. Order of Operations

1. Never start a task out of order. `TASKS.md` is sequenced so that later modules depend on earlier ones being real and tested (e.g., POS's checkout task assumes Catalog and Inventory already exist and work).
2. Within a task, follow this loop: **write the migration → write the entity/repository → write the service/facade → write the controller/DTOs → write tests → run the full test suite → update docs if behavior differs from what's written here.**
3. Do not move to the next task until the current one's Definition of Done (Section 6) is fully met.

## 2. Module Boundary Discipline

This is the single most important rule in this codebase — re-read `ARCHITECTURE.md` Sections 3–6 before writing any code that touches more than one module.

- If you're about to import a class from another module's `domain`, `repository`, `service`, or `web` package — stop. That's a violation. Either the data you need should come through that module's `Facade`, or the two modules are actually one module and the design needs revisiting (flag this rather than silently working around it).
- If you're about to write a JPA `@ManyToOne`/`@OneToMany` that crosses a module boundary — stop. Store the related ID as a plain UUID field instead, and resolve it via a Facade call when you need the related data.
- Run `mvn test -Dtest=ModuleBoundaryTest` before considering any task involving cross-module interaction complete.

## 3. Coding Standards

- **Immutability first**: DTOs are Java `record`s. Entities are mutable (JPA requires it) but keep setters package-private where possible, exposing behavior through named methods instead (`sale.completePayment(...)`, not `sale.setStatus(COMPLETED)`).
- **No business logic in controllers.** Controllers do request/response mapping and call a service/facade method. All business rules live in `service`.
- **No business logic in JPA entities beyond simple invariants.** Complex workflow logic (e.g., "applying a return") lives in a service class that orchestrates one or more entities.
- **Every public service method that can fail in an expected way throws a specific exception** (e.g., `InsufficientStockException`), never a generic `RuntimeException` — the global error handler in `API.md` Section 4 depends on this.
- **No magic strings for status/enum-like fields** — use Java enums, mapped to Postgres `text` columns via `@Enumerated(EnumType.STRING)` (never `ORDINAL` — it breaks on reordering).
- **Money is `long` (minor units) everywhere in Java code.** Never introduce `BigDecimal` or `double` for a monetary amount. Quantities (e.g., weighed products) do use `BigDecimal` since they're not currency.
- **Package-private by default.** A class is `public` only if another module's `api` package needs it, or if it's a JPA entity/repository Spring needs to see.

## 4. Testing Bar

- **Unit tests** for all service-layer business logic (discount calculation, FIFO cost consumption, journal-entry balancing, etc.) — no Spring context required, plain JUnit + Mockito for dependencies.
- **Integration tests** for every controller endpoint, using `@SpringBootTest` + Testcontainers Postgres (never H2 — H2's SQL dialect differences have hidden real bugs in past Postgres-backed projects; if it doesn't run against real Postgres, it isn't tested).
- **Every module's Facade methods** get an integration test exercised the way another module would actually call them.
- **Every event listener** gets a test proving it's idempotent — publish the same event twice, assert the side effect happened once.
- Target: every task in `TASKS.md` ships with tests covering its acceptance criteria before being marked complete. Do not write code first and "add tests later" as a separate task.

## 5. Git & Commit Discipline

- One task from `TASKS.md` = one feature branch = one PR (or one commit series if working directly). Do not bundle two unrelated tasks into one PR.
- Commit messages: `<module>: <what changed>` (e.g., `inventory: add FIFO cost layer consumption on stock_movement insert`).
- A PR/commit that fails `mvn verify` (which runs the full test suite + ArchUnit checks) is not done, regardless of how the feature behaves manually.

## 6. Definition of Done (apply to every task)

A task in `TASKS.md` is complete only when **all** of the following are true:

- [ ] Flyway migration(s) added, reviewed against `DATABASE.md` Section 12's checklist.
- [ ] Entities, repositories, service/facade, controller implemented per Sections 2–3 above.
- [ ] Module boundary rules held — `ModuleBoundaryTest` passes.
- [ ] Unit tests for business logic pass.
- [ ] Integration tests (Testcontainers) for new endpoints pass.
- [ ] OpenAPI docs are auto-generated and reviewed (`/swagger-ui.html`) — descriptions added for non-obvious fields.
- [ ] Error responses for expected failure cases match the format in `API.md` Section 4, with a specific `code`.
- [ ] `mvn verify` passes clean, including ArchUnit, on the full project — not just the new module.
- [ ] If this task's implementation revealed the spec in `DATABASE.md`/`API.md`/`ARCHITECTURE.md` was wrong or incomplete, that doc is updated in the same PR — the docs must never silently drift from the code.

## 7. When Something in the Spec Doesn't Make Sense

If a task in `TASKS.md` conflicts with the schema in `DATABASE.md`, or an API convention in `API.md` doesn't fit a specific case — don't silently improvise. Note the conflict, propose the smallest resolution that keeps the module-boundary rules intact, apply it, and update the relevant doc in the same change so the docs stay authoritative.
