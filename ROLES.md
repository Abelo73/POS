# NovaPOS — User Roles Architecture

This document is the higher-level design of **who uses NovaPOS and what they actually do in it** — it sits above `DATABASE.md`'s `user_access` schema and `API.md`'s authorization section, and below the PRD's one-line role summary. Read it before implementing Phase 1, Tasks 1.2–1.6 in `TASKS.md`; the role seed data and permission checks in those tasks come directly from this document.

---

## 1. Scope Model — How Roles Nest

Every role operates at exactly one of four scope levels. A role's scope determines what data it can see, not just what buttons it can click — this is enforced in the JWT claims and the `PermissionEvaluator`, per `API.md` Section 5.

```
PLATFORM
  └── COMPANY  (one business, e.g. "Amara's Retail Group")
        └── REGION  (an assigned group of branches — optional layer)
              └── BRANCH  (one physical or logical location)
```

- **Platform scope**: sees and manages across every company on NovaPOS. Only Anthropic-of-NovaPOS-style internal operations roles sit here (Super Admin, Support Engineer).
- **Company scope**: sees everything within one company, across all its branches.
- **Region scope**: sees a subset of branches within one company, assigned explicitly (e.g., "Southern Region: Branches 4, 7, 9").
- **Branch scope**: sees only one branch's data.

A user can hold **different roles at different scopes simultaneously** (e.g., someone who is Branch Manager of Branch 3 and also Purchasing Officer at company scope) — `user_role_assignment` in `DATABASE.md` models this with a nullable `branch_id` per assignment, not a single role-per-user field.

---

## 2. Role Directory

Seventeen roles, grouped by who they work for.

### 2.1 Platform Operations (NovaPOS-internal, not a customer's staff)

#### Super Admin
- **Scope**: Platform.
- **What they do**: full technical and business access across every company account on NovaPOS — used for platform operations, billing escalations, and emergency intervention. Not a role any customer's staff ever holds.
- **Typical tasks**: investigate a cross-company platform incident; adjust a company's subscription tier; impersonate-with-audit-trail to reproduce a customer-reported bug.
- **Boundary**: every action is audit-logged with full detail (Task 12.3); this role's use is expected to be rare and always traceable.

#### Support Engineer
- **Scope**: Platform, but time-boxed and scoped to a single company per support ticket.
- **What they do**: gets temporary, logged access to one customer's account to investigate and resolve a support ticket.
- **Typical tasks**: reproduce a reported bug in a customer's actual data (read-heavy); make a narrowly-scoped correction (e.g., un-stick a wrongly-held invoice) with the customer's consent.
- **Boundary**: access auto-expires when the ticket closes or after a fixed window, whichever comes first; cannot be granted indefinitely.

### 2.2 Company Leadership

#### Company Owner
- **Scope**: Company (all branches).
- **What they do**: the top of the business's own hierarchy. Full control of the company's configuration, staff, branches, and finances.
- **Typical tasks**: open a new branch; set company-wide tax/discount-stacking defaults; review consolidated P&L across all branches; hire/deactivate a Branch Manager; approve large one-off discretionary discounts escalated from a branch.
- **Boundary**: cannot see other companies' data (unlike Super Admin) — company scope is a hard ceiling.

#### Regional Manager
- **Scope**: Region (an assigned subset of branches within one company).
- **What they do**: oversight and performance management across a group of branches, without full company authority (can't change company-wide settings or billing).
- **Typical tasks**: compare weekly sales across their assigned branches; approve a Branch Manager's request for an out-of-policy return; escalate a supply issue affecting multiple branches in their region to Purchasing.
- **Boundary**: cannot view or act on branches outside their assigned region.

### 2.3 Branch Operations

#### Branch Manager
- **Scope**: Branch (one).
- **What they do**: full operational authority over a single branch — staff, pricing overrides within policy, approvals, daily closing.
- **Typical tasks**: approve a return outside the standard window; override a price at checkout; open/close the branch's daily reconciliation; hire/deactivate a Cashier at their branch; review the branch's low-stock alerts and nudge Purchasing.
- **Boundary**: cannot see or affect other branches; cannot change company-wide tax/discount policy, only branch-level overrides where the company allows them.

#### Cashier
- **Scope**: Branch, POS surface only.
- **What they do**: the highest-frequency role in the system — runs the checkout counter.
- **Typical tasks**: build a cart, apply eligible discounts, take payment (including split payment), hold/resume a sale, process an in-policy return, open the cash drawer at shift start, count and close their drawer at shift end.
- **Boundary**: cannot edit the product catalog, cannot see other cashiers' sales by default, cannot approve a return outside policy or apply a discount beyond their permitted ceiling — those escalate to Branch Manager.

#### Salesperson
- **Scope**: Branch.
- **What they do**: customer-facing sales support distinct from checkout — quotes, B2B orders, commission-tracked sales, often for higher-touch or wholesale-style transactions.
- **Typical tasks**: build a quote for a customer, convert an accepted quote into a sales order, follow up on a back-order, track their own commission-eligible sales.
- **Boundary**: does not process payment at a register the way a Cashier does (though the same person can hold both roles); cannot approve their own commission calculations.

### 2.4 Inventory & Warehouse

#### Inventory Officer
- **Scope**: one or more branches (assignable).
- **What they do**: owns stock accuracy at the branches they're assigned to.
- **Typical tasks**: receive review of low-stock alerts, approve or adjust reorder suggestions, run cycle counts, investigate and resolve negative-stock exceptions flagged from offline sync, review near-expiry batch alerts and coordinate clearance.
- **Boundary**: can adjust stock via documented adjustment reasons only (never a silent on-hand edit — every change is a `stock_movement` row); cannot approve their own large adjustment above a variance threshold without Branch Manager sign-off.

#### Storekeeper
- **Scope**: one warehouse.
- **What they do**: the physical hands-on role inside a warehouse (distinct from a selling branch).
- **Typical tasks**: receive incoming goods and put them away, pick items for an outgoing transfer, mark a transfer as picked/dispatched.
- **Boundary**: operates within one warehouse only; cannot approve a transfer, only execute the picking/receiving steps of one already approved.

#### Warehouse Manager
- **Scope**: one warehouse (oversight).
- **What they do**: owns warehouse-level KPIs and approves the transfers Storekeepers execute.
- **Typical tasks**: approve an inter-branch transfer request, review warehouse capacity and throughput, escalate a persistent receiving-variance pattern with a specific supplier to Purchasing.
- **Boundary**: does not have visibility into a selling branch's POS activity, only stock movements that touch their warehouse.

### 2.5 Purchasing & Finance

#### Purchasing Officer
- **Scope**: company or a set of branches (assignable).
- **What they do**: owns the buy-side relationship with suppliers.
- **Typical tasks**: create and send purchase orders (manually or from an auto-generated reorder draft), negotiate/record supplier payment terms, review and act on receiving variances, evaluate supplier lead-time performance.
- **Boundary**: cannot approve their own purchase order above a configured value threshold — that requires Branch Manager or Company Owner sign-off, depending on the amount.

#### Accountant
- **Scope**: Company.
- **What they do**: owns the financial system of record — the only role with full access to the Accounting module, explicitly without operational POS access.
- **Typical tasks**: review automatically-posted journal entries from sales/returns/purchases, reconcile receivables/payables aging, generate trial balance/P&L/balance sheet/cash flow for a period, investigate a discrepancy flagged between operational and financial data.
- **Boundary**: cannot process a POS sale or approve a stock adjustment — their access is financial, not operational, by design, which is itself a separation-of-duties control.

#### HR Manager
- **Scope**: Company.
- **What they do**: manages staff records and shift-adjacent data (not full payroll processing, which is typically an external/integrated system, but the data NovaPOS needs to support shift management and performance reporting).
- **Typical tasks**: onboard a new staff record (before/alongside their `user_access` account creation), manage shift schedules and handover records, review shift-discrepancy (over/short cash) reports by employee.
- **Boundary**: does not have transactional POS, inventory, or accounting access — HR Manager's access is staff-data-scoped.

### 2.6 Oversight

#### Auditor
- **Scope**: Company, read-only.
- **What they do**: independent read access across financial and operational logs for compliance or investigation purposes.
- **Typical tasks**: review the audit log for a specific time window or user, cross-check a sample of sales against journal entries, verify a Branch Manager's discount overrides stayed within policy over a quarter.
- **Boundary**: cannot write, edit, approve, or trigger any action anywhere in the system — a violation of this (any write attempt succeeding) is a critical bug, not an edge case.

### 2.7 System Configuration

#### System Administrator
- **Scope**: Company.
- **What they do**: owns technical configuration distinct from business operations — integrations, user provisioning mechanics, technical settings.
- **Typical tasks**: configure a payment gateway integration, manage API keys for third-party integrations, set up SSO if applicable, configure notification channel credentials (SMS/email provider).
- **Boundary**: distinct from Company Owner — has technical/settings authority but not automatic authority over financial data or discount policy unless separately granted.

### 2.8 External Parties (Self-Service Portals)

#### Customer
- **Scope**: their own records only.
- **What they do**: self-service view of their own relationship with the business.
- **Typical tasks**: view past orders and receipts, check loyalty point balance and store credit, view/redeem an available coupon.
- **Boundary**: cannot see any other customer's data, any operational or financial data, or any staff-facing screen.

#### Supplier
- **Scope**: their own records only.
- **What they do**: self-service view of their relationship with the business as a vendor.
- **Typical tasks**: view open purchase orders addressed to them, confirm/update expected delivery dates, submit an invoice against a received PO.
- **Boundary**: cannot see the company's inventory levels, other suppliers, or any customer-facing data — only POs and transactions involving them specifically.

---

## 3. Permission Matrix (Summary)

Full module-by-module detail lives in the companion technical documentation; this is the quick-reference cross-check.

| Role | Own-branch POS | Other branches | Inventory write | Purchasing | Accounting | User mgmt | Company settings |
|---|---|---|---|---|---|---|---|
| Super Admin | ✅ (any) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Company Owner | ✅ | ✅ (own co.) | ✅ | ✅ | View | ✅ (own co.) | ✅ |
| Regional Manager | View | View (region) | View | View | View | – | – |
| Branch Manager | ✅ | – | ✅ (own branch) | View | View | ✅ (own branch staff) | Branch-level only |
| Cashier | ✅ (own sales) | – | – | – | – | – | – |
| Salesperson | Quotes/orders | – | View | – | – | – | – |
| Inventory Officer | – | Assigned only | ✅ | View | – | – | – |
| Storekeeper | – | Assigned warehouse | Execute only | – | – | – | – |
| Warehouse Manager | – | Assigned warehouse | Approve | View | – | – | – |
| Purchasing Officer | – | Assigned | View | ✅ | View | – | – |
| Accountant | – | View (all) | View | View | ✅ | – | – |
| HR Manager | – | – | – | – | – | Staff records only | – |
| Auditor | View | View | View | View | View | View | View |
| System Administrator | – | – | – | – | – | Provisioning only | Technical settings |
| Customer | Own orders only | – | – | – | – | – | – |
| Supplier | Own POs only | – | – | View own | – | – | – |

---

## 4. How This Maps to the Backend Architecture

- **JWT claims**: at login (`ARCHITECTURE.md`/`API.md`), the token embeds the caller's `userId`, and their `roleAssignments` array (`role`, `scope: PLATFORM|COMPANY|REGION|BRANCH`, `scopeId`). This is what `TASKS.md` Task 1.2 seeds and Task 1.3 issues.
- **PermissionEvaluator**: on every request, checks (a) does this role have this permission on this module/action, and (b) does the requested resource's `branchId`/`companyId` fall within the caller's scope. Both checks must pass — a role with the right permission but wrong scope is still rejected (`Task 1.5`).
- **Separation of duties is structural, not just configured**: notice Accountant has no POS/Inventory write access, and Auditor has no write access anywhere — these aren't just default settings an admin could misconfigure away, they reflect the module boundary rules in `ARCHITECTURE.md`: the Accounting module's write endpoints simply don't grant that permission to the Accountant role in the seed data, and the seed data is the single source of truth checked in Task 1.2.
- **Scope hierarchy in practice**: a Regional Manager's `scope: REGION` claim doesn't map to a single `branchId` — it maps to a `region_id` that resolves (via `CompanyFacade`) to a list of branch IDs at request time, so adding a branch to a region doesn't require reissuing every Regional Manager's token.

---

## 5. Design Principle Behind This Whole Document

Every role above is defined by **what they need to do their job, and nothing else** — not by convenience, and not by "give them access in case they need it later." A role that doesn't need write access to a module doesn't get it configured off; it's built to never have the permission to request it. This is what makes the permission matrix in Section 3 something the system enforces by construction, not something an administrator has to remember to configure correctly for every new hire.
