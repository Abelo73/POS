# NovaPOS Backend — Task List (Build Order)

Work through this top to bottom. Do not skip ahead — later phases assume earlier ones are done and tested. Every task's Definition of Done is the checklist in `SKILLS.md` Section 6, applied on top of that task's specific acceptance criteria below.

---

## Phase 0 — Project Setup

**Task 0.1 — Initialize the project**
- Generate a Spring Boot 3.3.x Maven project (Java 21) with dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `postgresql` driver, `flyway-core`, `springdoc-openapi-starter-webmvc-ui`, `jjwt-api`/`jjwt-impl`/`jjwt-jackson`, `archunit-junit5` (test scope), `testcontainers` + `testcontainers-postgresql` (test scope), `junit-jupiter`, `mockito-core`.
- Set up the package skeleton exactly as in `README.md` Section "Repository Structure".
- Acceptance: `mvn spring-boot:run` starts a blank app successfully with no errors.

**Task 0.2 — Local Postgres via Docker Compose**
- Add `docker-compose.yml` with a `postgres:16` service, exposing 5432, with a named volume, and default schemas (`company`, `user_access`, `catalog`, `inventory`, `pos`, `purchasing`, `customer`, `promotion`, `accounting`, `restaurant`) created via an init SQL script.
- Acceptance: `docker compose up -d postgres` + `mvn spring-boot:run` connects successfully.

**Task 0.3 — Flyway wired per module**
- Configure Flyway with multiple `locations` (one per module folder under `db/migration`), per `DATABASE.md` Section 1.
- Acceptance: an empty baseline migration per module applies cleanly on startup.

**Task 0.4 — ArchUnit module boundary test scaffold**
- Create `src/test/java/com/novapos/architecture/ModuleBoundaryTest.java` implementing the rules in `ARCHITECTURE.md` Section 6, even before real modules exist (rules should just pass vacuously).
- Acceptance: `mvn test -Dtest=ModuleBoundaryTest` passes and fails intentionally if you temporarily add a violating import (verify this, then remove it).

**Task 0.5 — Global error handling & response envelope**
- Implement `@RestControllerAdvice` per `API.md` Section 4. Include a base `NovaPosException` (abstract) with `code`, `message`, `details`, and an HTTP status mapping.
- Acceptance: a test controller throwing a sample exception returns the documented JSON shape and correct status code.

**Task 0.6 — OpenAPI wired**
- Add springdoc config; confirm `/swagger-ui.html` loads in `local` profile.
- Acceptance: Swagger UI loads with zero controllers and shows the base error schema.

**Task 0.7 — CI pipeline skeleton**
- Add a CI config (GitHub Actions or equivalent) running `mvn verify` (unit + integration tests + ArchUnit) on every push, using Testcontainers (requires Docker-in-CI).
- Acceptance: a trivial PR triggers CI and passes.

---

## Phase 1 — Foundation Modules: Company, Branch, User & Access

**Task 1.1 — Company & Branch module**
- Implement `company` module per `DATABASE.md` Section 2: entities, repository, `CompanyFacade`/`BranchFacade`, controllers for CRUD on company/branch.
- Business rule: a company must have at least one branch (enforce in service layer, not just DB).
- Acceptance: create company → create branch → fetch branch scoped by company, all via API; unit test for the "at least one branch" rule; integration test for the endpoints.

**Task 1.2 — User & Access module: identity**
- Implement `user_access` schema entities (`app_user`, `role`, `user_role_assignment`), repository, `UserFacade`.
- Seed the fixed role set from the permission matrix (Super Admin, Company Owner, Regional Manager, Branch Manager, Cashier, Inventory Officer, Purchasing Officer, Accountant, Auditor, etc.) via a Flyway data migration.
- Acceptance: create a user, assign a role scoped to a branch, fetch effective roles for a user.

**Task 1.3 — Authentication: credential login + JWT**
- Implement `/api/v1/auth/login` (email/password → JWT access + refresh token), password hashing with BCrypt, JWT signing/validation per `API.md` Section 5.
- Acceptance: login with valid credentials returns a token; invalid credentials return 401 with the standard error shape; token contains role/branch claims.

**Task 1.4 — PIN login for shared terminals**
- Implement `/api/v1/auth/pin-login`, requiring a registered `terminalId` (add a minimal `terminal` table to `user_access` schema: id, branch_id, device_fingerprint, registered_at).
- Acceptance: PIN login succeeds only for a PIN belonging to a user with an active role at the terminal's branch; fails otherwise with a specific error code.

**Task 1.5 — Authorization enforcement**
- Implement the `PermissionEvaluator` and `@PreAuthorize` wiring described in `API.md` Section 5. Apply it to every endpoint created so far.
- Acceptance: a Cashier-role token gets 403 on a Branch-Manager-only endpoint (e.g., branch settings update); a Branch Manager token from Branch A gets 403 accessing Branch B's data.

**Task 1.6 — MFA for elevated roles**
- Implement TOTP-based MFA enrollment/verification; enforce as required at login for Company Owner, Accountant, System Administrator roles (configurable flag per role, defaulting per `DATABASE.md`/PRD).
- Acceptance: login for an MFA-required role without a valid TOTP code is rejected with a distinct "MFA required" response the client can act on.

---

## Phase 2 — Catalog Module

**Task 2.1 — Products, categories, brands**
- Implement `catalog` schema entities/repositories/`CatalogFacade`, CRUD controllers for category, brand, product, product_variant.
- Business rule: `(company_id, sku)` uniqueness; deleting a product is a soft delete only.
- Acceptance: create a category tree, a brand, a product with two variants, confirm uniqueness constraint returns 409/422 correctly on a duplicate SKU.

**Task 2.2 — Branch price overrides**
- Implement `branch_price_override`; `CatalogFacade.getEffectivePrice(productVariantId, branchId)` resolves override → base price.
- Acceptance: unit test confirms override wins when present, base price used otherwise.

**Task 2.3 — Composite/bundle products**
- Support `is_composite` products with a components mapping table (add migration); `CatalogFacade` exposes component breakdown for a bundle SKU.
- Acceptance: creating a bundle and resolving its components returns the correct list.

---

## Phase 3 — Inventory Module

**Task 3.1 — Stock movement ledger**
- Implement `inventory.stock_movement`, repository, and `InventoryFacade.recordMovement(...)`. On-hand quantity is always computed via a `sum(quantity_delta)` query (or a maintained materialized/derived view) — never stored as a directly-editable column.
- Acceptance: recording a sequence of movements yields the correct on-hand quantity; attempting to "edit" a past movement is not possible via the API (no update endpoint exists for stock_movement — only inserts).

**Task 3.2 — Costing methods (FIFO/LIFO/Average)**
- Implement cost-layer tracking for FIFO/LIFO (consume layers in order) and moving-average calculation, configurable per company/product per `catalog.product.costing_method`.
- Acceptance: unit tests for all three methods against a scripted sequence of receipts and consumptions, verifying both remaining on-hand and COGS per consumption match hand-calculated expected values.

**Task 3.3 — Batch, expiry, serial, lot tracking**
- Implement `inventory.batch`; extend `recordMovement` to accept an optional batch reference; add a near-expiry query (`InventoryFacade.findBatchesExpiringBefore(date)`).
- Acceptance: receiving two batches of the same variant with different expiry dates, then selling, consumes the earlier-expiring batch first when FIFO-by-expiry is configured.

**Task 3.4 — Reorder levels & low-stock alerts**
- Add `reorder_level`/`reorder_quantity` fields (migration on a per-branch product settings table); implement a scheduled check (Spring `@Scheduled` or event-driven on every movement) that publishes a `LowStockDetectedEvent` when on-hand crosses at/below reorder level.
- Acceptance: integration test proves the event fires exactly once per crossing (not repeatedly while still below threshold).

**Task 3.5 — Transfers between locations**
- Implement `transfer_order`/`transfer_line` with status lifecycle (requested → approved → picked → in_transit → received); stock leaves source on dispatch, enters destination only on confirmed receipt.
- Acceptance: integration test walks a transfer through the full lifecycle, asserting on-hand at source and destination at each step, including the in-transit state where neither location shows the stock as available.

**Task 3.6 — Negative stock policy**
- Add a branch-level config flag (`allow_negative_stock`); `InventoryFacade.reserveStock(...)` either blocks or allows-and-flags per this setting, publishing a `NegativeStockExceptionEvent` when it occurs.
- Acceptance: both branch configurations tested explicitly.

**Task 3.7 — Cycle counting & physical inventory**
- Implement a `stock_count` + `stock_count_line` structure (add migration) supporting partial (cycle) and full counts; submission generates `stock_movement` adjustment entries for variance, requiring approval above a configured variance threshold.
- Acceptance: a count with variance below threshold auto-applies; above threshold stays pending until an approval endpoint is called.

---

## Phase 4 — POS Module (Core Checkout)

**Task 4.1 — Cart & sale creation**
- Implement `pos.sale`/`pos.sale_line`, `PosFacade`/service for building a cart (add/remove line, resolve price via `CatalogFacade`, resolve tax — stub tax as flat-rate per branch config for now, full Tax handling can be a follow-up task if not yet modeled).
- Acceptance: build a multi-line cart via API, confirm subtotal/tax/total calculations are correct.

**Task 4.2 — Payments & split payment**
- Implement `pos.payment`; support multiple payment rows per sale; validate sum(payments) == total before allowing completion.
- Acceptance: a split cash+card sale completes only when amounts reconcile; an under/over payment attempt returns 422 with a specific code.

**Task 4.3 — Sale completion side effects**
- On sale completion: publish `SaleCompletedEvent`; `InventoryFacade` listener decrements stock; `AccountingFacade` listener posts the journal entry (Phase 7 must exist first for this to be real — until then, stub/log the accounting call and revisit once Phase 7 is built).
- Acceptance: completing a sale decrements inventory correctly (idempotency test: replaying the same event does not double-decrement).

**Task 4.4 — Hold & resume**
- Implement `held` status on `sale`; endpoints to hold and resume; configurable expiry (default 24h) with a scheduled job releasing expired holds and any reserved stock.
- Acceptance: hold, resume from a different (authorized) session, and expiry-release all tested.

**Task 4.5 — Returns, refunds, exchanges**
- Implement `pos.return_line`; return workflow validates returned quantity ≤ original purchased quantity per line; restocks inventory via `InventoryFacade`; publishes `SaleReturnedEvent`.
- Acceptance: full-line and partial-line returns tested; over-return attempt rejected with a specific error code.

**Task 4.6 — Idempotent offline sale ingestion**
- Implement the `client_uuid`-based idempotency described in `DATABASE.md` Section 6 / `API.md` Section 6 — a sale POST with a previously-seen `client_uuid` and identical payload returns the original result; different payload returns 409.
- Acceptance: integration test simulates a terminal retry after a dropped response, confirms no duplicate sale is created.

**Task 4.7 — "Complimentary" zero-total sale override**
- Enforce the business rule that a completed sale cannot total ≤ $0 without a manager-approved flag; implement the approval check against the caller's role.
- Acceptance: a $0 sale without the flag is rejected; with the flag and a Branch-Manager-or-above token, it succeeds.

---

## Phase 5 — Purchasing Module

**Task 5.1 — Suppliers**
- CRUD for `purchasing.supplier`.
- Acceptance: standard CRUD integration test.

**Task 5.2 — Purchase orders & approval**
- Implement PO creation (draft), approval workflow (role-gated), and status transitions.
- Acceptance: a PO cannot move to `approved` without the correct role; state machine transitions tested exhaustively (valid and invalid transitions).

**Task 5.3 — Receiving with variance**
- Implement receiving against a PO line, allowing partial quantity; on receipt, call `InventoryFacade.recordMovement` (reason = `receipt`) and publish `PurchaseReceivedEvent` for Accounting to post a payable.
- Acceptance: receiving less than ordered updates PO status to `partially_received` and Inventory reflects exactly the received quantity, per the Module Specifications scenario.

**Task 5.4 — Auto-generated PO from low-stock alert**
- Subscribe to `LowStockDetectedEvent` (Phase 3.4); when enabled per branch/product config, auto-create a draft PO to the product's default supplier.
- Acceptance: triggering the low-stock event with auto-reorder enabled results in a draft PO; disabled config results in none.

---

## Phase 6 — Customers, CRM, and the Discount/Promotion/Loyalty Engine

**Task 6.1 — Customer profiles**
- CRUD for `customer.customer`; duplicate-merge endpoint.
- Acceptance: standard CRUD + merge test (merging moves loyalty/store-credit ledger entries to the surviving record).

**Task 6.2 — Loyalty ledger**
- Implement `loyalty_ledger` with accrual (on `SaleCompletedEvent`, per configured earn rate) and redemption (called from POS during checkout via `CustomerFacade.redeemPoints(...)`).
- Acceptance: accrual and redemption both produce correct ledger entries and balances; redemption exceeding balance is rejected.

**Task 6.3 — Store credit**
- Implement `store_credit_ledger`; issued on return-to-credit (Phase 4.5 hook), usable as a POS payment method.
- Acceptance: issuing credit on a return and spending it on a later sale both reflected correctly, balance never goes negative.

**Task 6.4 — Promotion engine**
- Implement `promotion.promotion` with `rules` jsonb evaluated against a cart (date range, branch/product/segment scope); `PromotionFacade.evaluate(cart)` returns applicable discounts.
- Acceptance: a scripted set of promotions (percentage, BOGO, threshold) each tested against carts that should and shouldn't qualify.

**Task 6.5 — Coupons & gift cards**
- Implement `coupon` redemption validation (max redemptions, expiry) and `gift_card` balance issuance/redemption as a POS payment method.
- Acceptance: expired/over-redeemed coupon rejected with specific codes; gift card balance decremented correctly on use, cannot go negative.

**Task 6.6 — Stacking policy**
- Implement the configurable stacking resolution described in Module Specifications (automatic promos + one coupon, or "best discount only"), applied inside `PromotionFacade.evaluate(cart)`.
- Acceptance: both stacking policies tested against the same cart, confirming different (correct) outcomes.

---

## Phase 7 — Accounting Module

**Task 7.1 — Chart of accounts**
- CRUD for `accounting.account`; seed a default retail-focused chart via Flyway data migration.
- Acceptance: default chart loads; custom accounts can be added.

**Task 7.2 — Journal posting interface**
- Implement `AccountingFacade.postJournalEntry(entryRequest)` enforcing balanced debit/credit (Section 10 DB check as a safety net, application-level validation as the primary guard with a clear error).
- Acceptance: an unbalanced entry attempt is rejected before hitting the DB; a balanced entry posts correctly.

**Task 7.3 — Wire up automatic postings**
- Go back and complete the stubbed Accounting calls from Task 4.3 (sale completion), Task 4.5 (returns), Task 5.3 (purchase receipts) — each now posts a real, correct journal entry.
- Acceptance: for each of the three flows, assert the exact expected debit/credit lines against the accounts touched (revenue, tax payable, COGS, inventory, cash/card clearing, accounts payable, store credit liability, etc., matching the scenarios in the Module Specifications document).

**Task 7.4 — Financial statements**
- Implement trial balance, balance sheet, P&L, cash flow report endpoints, scoped by branch or consolidated, for a date range.
- Acceptance: statements generated against a seeded set of journal entries match hand-calculated expected totals.

**Task 7.5 — Receivables/payables aging**
- Implement aging bucket calculation (30/60/90+) for outstanding customer/supplier balances.
- Acceptance: seeded invoices at known ages return the correct bucket assignment.

---

## Phase 8 — Restaurant Operations (Optional Module — build if targeting that vertical)

**Task 8.1 — Tables & reservations**
- CRUD for `dining_table`, reservation hold/release with no-show expiry.
- Acceptance: reservation check-in converts to seated status; no-show past grace period releases automatically.

**Task 8.2 — Menu & recipes**
- Implement `menu_item`/`recipe_line`; selling a menu item decrements ingredient-level inventory via `InventoryFacade`, not a finished-goods SKU.
- Acceptance: selling a menu item with a modifier that changes ingredient quantity decrements the correct adjusted amounts (per the "no onions" scenario in Module Specifications).

**Task 8.3 — Kitchen order routing**
- Implement order-to-station routing and aggregated ticket status.
- Acceptance: an order with items split across two stations only reports "ready" once both stations complete.

**Task 8.4 — Split billing**
- Implement splitting a table's accumulated order into multiple POS sales.
- Acceptance: a table split two ways produces two independent, correctly-totaled sales, and releases the table only once both are settled.

---

## Phase 9 — Reporting Module

**Task 9.1 — Read-only reporting facade**
- Implement `reporting` module reading via other modules' Facades (never direct schema access) for: sales by branch/product/category/cashier, inventory valuation, P&L summary, employee performance.
- Acceptance: each standard report endpoint returns correct aggregates against seeded data.

**Task 9.2 — Role-scoped dashboards**
- Implement dashboard endpoints returning KPI sets per role, automatically scoped to the caller's branch/company permission per Phase 1.5.
- Acceptance: a Branch Manager's dashboard call never returns another branch's figures, verified by test.

---

## Phase 10 — Notifications Module

**Task 10.1 — Event-subscribed dispatch**
- Implement `notification` module subscribing to `SaleCompletedEvent` (receipt), `LowStockDetectedEvent` (alert), and any other events defined so far; integrate one real channel (email via SMTP or a provider SDK) and stub others (SMS/WhatsApp) behind the same interface.
- Acceptance: completing a sale triggers exactly one receipt email in a test using a fake mail sender; a low-stock event triggers exactly one alert.

---

## Phase 11 — Offline Mode & Sync Hardening (POS)

**Task 11.1 — Batch sync endpoint**
- Implement a batch ingestion endpoint accepting an ordered list of offline-queued sales (each with its own `client_uuid`), processing idempotently per Task 4.6, and returning per-item results (success, duplicate, or conflict) so the terminal knows what to retry.
- Acceptance: a batch with a mix of new, duplicate, and conflicting entries returns the correct per-item outcome for each.

**Task 11.2 — Negative-stock exception review queue**
- Implement an endpoint listing open negative-stock exceptions (Task 3.6) for Inventory Officer review and resolution.
- Acceptance: exceptions raised during simulated concurrent offline sales appear in the queue and can be marked resolved.

---

## Phase 12 — Final Hardening (Before UI Work Begins)

**Task 12.1 — Full ArchUnit sweep**
- Run the complete module boundary suite across every module built; fix any violation that crept in during earlier phases.

**Task 12.2 — Rate limiting**
- Add rate limiting at the gateway/filter level for `/api/v1/auth/*` endpoints specifically (credential stuffing mitigation) per `API.md`/security requirements.

**Task 12.3 — Audit logging**
- Implement an audit log listener capturing sensitive actions (price override, void, refund, permission change, login) into an immutable `audit_log` table (new `shared` schema), append-only at the DB permission level.
- Acceptance: each listed action type produces exactly one audit entry with actor, action, target, and timestamp.

**Task 12.4 — End-to-end smoke test**
- Write one integration test walking the full Scenario A from the Module Specifications document (jacket + 2 T-shirts, promo, loyalty redemption, split payment) start to finish across POS → Inventory → CRM → Accounting, asserting final state in all four modules matches the documented scenario's numbers exactly.
- Acceptance: this test passes. This is the backend "done" gate — once it's green, proceed to UI work.

---

## After This List

Once Phase 12 is complete and green in CI, the backend is ready for the UI phase. Do not begin UI work before Task 12.4 passes — the PRD's modular-first sequencing depends on the backend being real and tested first.
