# NovaPOS — Target Markets, Applicability & Business-Type Separation

This document answers three questions the earlier docs assumed but never spelled out: **who is this system for, what kinds of businesses can run on it, and how does one codebase safely serve all of them without a pharmacy accidentally looking like a restaurant, or one company ever seeing another's data.** Read this alongside `ROLES.md` (who uses it) and `ARCHITECTURE.md` (how modules are built) — this doc is about who the *tenant* is, not who the *user* is.

---

## 1. Who This System Is For

NovaPOS is built for businesses that sell things in person (and often online) and have outgrown a bare-bones POS, but don't need — or can't afford — full enterprise ERP complexity. Three buyer profiles:

| Buyer profile | Size | What they need from NovaPOS |
|---|---|---|
| **Independent owner-operator** | 1–2 locations, 2–15 staff | Fast setup, low cost, everything (POS + inventory + basic accounting) in one login, no IT staff required |
| **Growing multi-branch chain** | 3–200 branches | Centralized control, consolidated reporting, role-based access, ability to onboard a new branch without a migration |
| **Franchise group / holding company** | Multiple owners under one brand | Branch-level (or franchisee-level) autonomy with brand-level oversight and standardized reporting upward |

A fourth profile sits underneath all three: **the individual staff member** (Cashier, Inventory Officer, etc.) — covered in full in `ROLES.md`. This document is about the business as a customer, not the person as a user.

---

## 2. Where This System Can Be Applied (Verticals)

NovaPOS's core (Products, Inventory, POS, Accounting, CRM, Purchasing, Multi-Branch, User Access, Reporting, Notifications) is vertical-agnostic. On top of that shared core, **optional modules and configuration profiles** adapt it to a specific kind of business. No vertical requires a different codebase or a fork — see Section 4.

### 2.1 Retail Shop / Boutique
Small-format general or specialty retail (clothing, gifts, books). Needs: variants (size/color), simple promotions, straightforward FIFO costing. Doesn't need: recipes, table management, batch/expiry tracking in most cases.

### 2.2 Supermarket / Grocery
High SKU count, high transaction volume, weighed items, frequent promotions, sometimes perishables. Needs: weight-scale integration, batch/expiry tracking for fresh goods, fast barcode-driven checkout, tight reorder-level automation given volume.

### 2.3 Restaurant / Cafe
Table service or quick service. Needs: the full Restaurant Operations module (tables, reservations, KDS, recipes/modifiers), tipping support, split billing. Recipe-based ingredient decrement replaces simple finished-goods stock tracking for prepared items.

### 2.4 Pharmacy
Regulated, safety-critical inventory. Needs: mandatory batch and expiry tracking (not optional here), strict negative-stock blocking (never allow selling what isn't verifiably in stock), tighter audit logging on controlled items, prescription-linked customer records where applicable.

### 2.5 Electronics Store
Higher unit price, warranty tracking, serial-number-level inventory. Needs: serial tracking (not just batch), longer return-window policy configuration, possibly installment/"on account" payment more heavily used than cash businesses.

### 2.6 Clothing Store
Heavy variant usage (size × color), seasonal promotion cycles, exchange-heavy returns (size swaps). Needs: strong variant modeling, promotion engine tuned for seasonal campaigns, exchange workflow (not just refund).

### 2.7 Wholesale / Distribution Business
B2B, not primarily walk-in retail. Needs: the Sales module's quote-to-order flow more than walk-up POS, tiered/customer-specific pricing, credit terms and receivables aging front-and-center, large-batch inventory movements, minimal or no Restaurant/KDS relevance.

### 2.8 Service Business (e.g., salon, repair shop)
Sells time/labor alongside or instead of physical goods. Needs: lighter inventory tracking (parts/supplies only), appointment-like scheduling (adjacent to, but distinct from, restaurant table management — a future module extension, not in Phase 1 scope per the PRD).

---

## 3. Module Enablement Matrix

Every module below is either **Core** (always on, every tenant), **Optional — commonly on** (on by default for that vertical, toggleable), or **Optional — off** (available but not relevant by default).

| Module | Retail | Supermarket | Restaurant | Pharmacy | Electronics | Clothing | Wholesale |
|---|---|---|---|---|---|---|---|
| POS / Sales | Core | Core | Core | Core | Core | Core | Optional (quote-led) |
| Inventory | Core | Core | Core | Core | Core | Core | Core |
| Products/Catalog | Core | Core | Core | Core | Core | Core | Core |
| Accounting | Core | Core | Core | Core | Core | Core | Core |
| Customers/CRM | Core | Optional-on | Optional-on | Core | Optional-on | Optional-on | Core |
| Purchasing | Core | Core | Core | Core | Core | Core | Core |
| Discount/Promotion/Loyalty | Optional-on | Optional-on | Optional-on | Optional-off | Optional-on | Optional-on | Optional-off |
| Batch/Expiry tracking | Optional-off | Optional-on | Optional-off | **Core (mandatory)** | Optional-off | Optional-off | Optional-on |
| Serial tracking | Optional-off | Optional-off | Optional-off | Optional-off | Optional-on | Optional-off | Optional-off |
| Restaurant Operations (tables/KDS/recipes) | Off | Off | **Core** | Off | Off | Off | Off |
| Multi-Branch | Optional-on | Optional-on | Optional-on | Optional-on | Optional-on | Optional-on | Optional-on |
| Weight-scale integration | Off | Optional-on | Off | Off | Off | Off | Optional-on |

This table is the product-level view of the `novapos.modules.<module>.enabled` configuration flags introduced in `ARCHITECTURE.md` Section 9 — each row is a real config key, not just a marketing toggle.

---

## 4. How Separation Actually Works (Architecture)

There are **two different kinds of separation** happening in this system, and it's important not to conflate them:

### 4.1 Tenant Separation (Company A can never see Company B)

This is data isolation between unrelated businesses sharing the same NovaPOS deployment.

- **Model**: shared database, shared schema-per-module (as defined in `DATABASE.md`), row-level isolation via a mandatory `company_id` (or `branch_id`, which resolves to a company) on every tenant-owned table.
- **Enforcement, in layers**:
  1. **Application layer**: every repository query is automatically scoped by the caller's `company_id` from their JWT claim — no query path exists that omits this filter (enforce via a base repository/query-interceptor pattern, not by remembering to add `WHERE company_id = ?` in every method by hand).
  2. **Database layer (defense in depth)**: enable PostgreSQL **Row-Level Security (RLS)** on every tenant-owned table, with a policy that compares the row's `company_id` against a session variable (`SET app.current_company_id = '...'`) set at the start of every request. Even a bug in the application layer's filtering cannot leak cross-tenant data if RLS is in place — add this as a hardening task in `TASKS.md` Phase 12 if not already covered.
  3. **No shared mutable global tables** — even reference-like data (e.g., a tax rate table) is company-scoped, because two companies in different jurisdictions must never share a row that one of them could edit.
- **Why not schema-per-tenant or database-per-tenant instead**: at the scale of "independent shop to a few hundred branches," row-level isolation in a shared schema is operationally far simpler (one set of migrations, one connection pool, one set of backups) and is the standard approach for this class of product. Database-per-tenant becomes worth revisiting only if a specific enterprise customer's compliance requirements demand physical isolation — that's a per-customer deployment decision, not a default architecture change.

### 4.2 Vertical / Business-Type Separation (a pharmacy shouldn't see restaurant features)

This is a **configuration and UI-surfacing concern, not a data-isolation concern** — a pharmacy tenant and a restaurant tenant are still just two rows in the same `company` table with different config.

- **Model**: a `company_settings` (or `branch_settings`) record holding the module-enablement flags from Section 3, plus vertical-specific parameters (e.g., `pharmacy.strict_negative_stock = true`, `restaurant.default_table_count = 12`).
- **Backend enforcement**: a disabled module's endpoints still exist (one codebase, per `ARCHITECTURE.md`), but a feature-flag check at the start of each disabled module's controller/service methods returns 403/404 rather than executing — the Restaurant module's endpoints simply refuse to operate for a company that hasn't enabled them, the same way a permission check refuses an unauthorized role.
- **Frontend surfacing** (once UI work begins, per your earlier build order): navigation and screens for a disabled module are not shown at all — this is a UX concern layered on top of the backend flag, not a substitute for the backend check. Never rely on hiding a button in the UI as the only protection; the backend must refuse the action regardless of what the UI shows.
- **Onboarding**: when a new company signs up, an onboarding step asks "what kind of business are you?" and applies a **vertical profile** — a predefined bundle of the Section 3 flags for that business type — as sensible defaults, which the Company Owner can then adjust individually. This is a one-time data-seeding operation (`INSERT` into `company_settings` from a profile template), not a structural difference in the codebase.

### 4.3 Summary: One Codebase, Two Independent Axes of Separation

```
                 Company A (Retail)     Company B (Pharmacy)     Company C (Restaurant)
Tenant isolation:  company_id = A         company_id = B            company_id = C        ← never crosses
Vertical config:   restaurant=off         restaurant=off            restaurant=ON
                   batch_tracking=off     batch_tracking=ON          batch_tracking=off
```

Tenant isolation answers "whose data is this." Vertical configuration answers "which parts of the shared system does this tenant's business actually use." They are enforced by different mechanisms (RLS/query-scoping vs. feature flags) and must not be implemented as the same check — a bug that conflates them is exactly the kind of thing `ModuleBoundaryTest`-style automated checks in `TASKS.md` should be extended to catch (add a `TenantIsolationTest` alongside it).

---

## 5. Worked Example: Onboarding Three Different Tenants

**A single clothing boutique (Retail vertical, single branch):**
`company_settings`: `restaurant=off`, `batch_tracking=off`, `serial_tracking=off`, `multi_branch=off`, `promotion_engine=on`, `crm=on`. Simple, fast setup — most of Section 3's optional-off modules stay off.

**A regional pharmacy chain (Pharmacy vertical, 14 branches):**
`company_settings`: `restaurant=off`, `batch_tracking=on (enforced, not user-toggleable given regulatory need)`, `serial_tracking=off`, `multi_branch=on`, `negative_stock_allowed=false at every branch by policy`, `promotion_engine=off` (pharmacies in this business's target regulatory environment restrict promotional pricing on medicine). Fourteen `branch` rows under one `company_id`, each inheriting these defaults with branch-specific tax-zone overrides only.

**A fast-casual restaurant group (Restaurant vertical, 6 branches, some also doing retail merchandise):**
`company_settings`: `restaurant=on`, `batch_tracking=off`, `weight_scale=off`, `multi_branch=on`, `promotion_engine=on`. Because this group also sells branded merchandise (t-shirts) at two locations, those two branches additionally enable the standard retail product-variant features — proving vertical configuration is per-branch-capable, not just a single company-wide switch, for businesses that are genuinely mixed-use.

---

## 6. Adding a New Vertical Later

Because verticals are configuration, not code forks, supporting a new business type (e.g., a hardware/DIY store, or a hair salon with service-appointment needs) is primarily a Section 3-style analysis exercise:

1. Identify which existing modules already cover it (usually most of the core does, unmodified).
2. Identify which existing optional modules should default on/off for it.
3. Identify whether it needs a genuinely new module (e.g., appointment scheduling for a salon isn't just a config flag on an existing module — that's new domain logic, and would follow the full module-creation process in `ARCHITECTURE.md`, becoming its own `com.novapos.<newmodule>` package with its own schema, Facade, and events, exactly like Restaurant Operations was built).
4. Ship it as a new vertical profile template (Section 4.2) once the module (if new) passes the same Definition of Done as any other module in `SKILLS.md`.
