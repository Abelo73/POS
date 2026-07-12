# NovaPOS Backend — Database (PostgreSQL)

## 1. Conventions

- **One PostgreSQL database, one schema per module** (`pos`, `inventory`, `catalog`, `accounting`, etc.) using Postgres native schemas — not just a naming prefix. This gives the module-boundary rule in `ARCHITECTURE.md` a real database-level enforcement point: a Postgres role can be granted access to only the schemas its service needs, and it's a straightforward step to give each module its own physical database at Phase 2 extraction time.
- **Flyway** manages migrations, one file set per module, named `db/migration/<module>/V<version>__<description>.sql` (e.g. `db/migration/inventory/V1__create_stock_movement.sql`). Flyway is configured with multiple locations, one per module schema.
- **Primary keys**: `uuid`, generated application-side (`UUID.randomUUID()`) or via `gen_random_uuid()` (pgcrypto), never serial/identity — avoids exposing sequential IDs and simplifies offline-client-generated IDs for POS (see `sale.client_uuid` below).
- **Money**: stored as `bigint` in minor units (cents), never `numeric`/`float` for currency math in application code — conversion to a display value happens at the API boundary only.
- **Timestamps**: `timestamptz`, always UTC. Every table has `created_at timestamptz not null default now()`, `updated_at timestamptz not null default now()` (updated via trigger or `@PreUpdate`), and `deleted_at timestamptz` nullable for soft delete.
- **No cross-schema foreign keys.** A table in the `pos` schema referencing a `catalog` product does so by storing the UUID with no DB-level FK constraint into another module's schema — referential integrity across modules is enforced in application code via Facade calls, not the database, because a DB-level FK is exactly the kind of physical coupling that blocks later extraction.

## 2. Schema: `company` (Module: Company & Branch Management)

```sql
create table company.company (
  id uuid primary key,
  name text not null,
  default_currency char(3) not null,
  billing_status text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table company.branch (
  id uuid primary key,
  company_id uuid not null references company.company(id),
  name text not null,
  timezone text not null,
  tax_zone text not null,
  currency char(3) not null,
  address jsonb,
  opening_hours jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);
create index idx_branch_company on company.branch(company_id);
```

## 3. Schema: `user_access` (Module: User & Access Management)

```sql
create table user_access.app_user (
  id uuid primary key,
  company_id uuid not null,
  email text unique,
  phone text,
  password_hash text,
  pin_hash text,
  mfa_enabled boolean not null default false,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table user_access.role (
  id uuid primary key,
  name text not null unique,          -- e.g. 'CASHIER', 'BRANCH_MANAGER'
  description text
);

create table user_access.user_role_assignment (
  id uuid primary key,
  user_id uuid not null references user_access.app_user(id),
  role_id uuid not null references user_access.role(id),
  branch_id uuid,                      -- null = company-wide scope
  created_at timestamptz not null default now()
);
create index idx_ura_user on user_access.user_role_assignment(user_id);
```

## 4. Schema: `catalog` (Module: Products, Categories, Brands)

```sql
create table catalog.category (
  id uuid primary key,
  company_id uuid not null,
  parent_id uuid references catalog.category(id),
  name text not null
);

create table catalog.brand (
  id uuid primary key,
  company_id uuid not null,
  name text not null
);

create table catalog.product (
  id uuid primary key,
  company_id uuid not null,
  sku text not null,
  name text not null,
  category_id uuid references catalog.category(id),
  brand_id uuid references catalog.brand(id),
  base_price_minor bigint not null,
  currency char(3) not null,
  tax_class text not null,
  track_inventory boolean not null default true,
  costing_method text not null default 'FIFO',  -- FIFO | LIFO | AVERAGE
  is_composite boolean not null default false,
  sold_by_weight boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  unique (company_id, sku)
);

create table catalog.product_variant (
  id uuid primary key,
  product_id uuid not null references catalog.product(id),
  variant_name text not null,          -- e.g. 'Medium / Blue'
  barcode text,
  price_override_minor bigint,
  created_at timestamptz not null default now(),
  deleted_at timestamptz
);
create index idx_variant_product on catalog.product_variant(product_id);

create table catalog.branch_price_override (
  id uuid primary key,
  product_id uuid not null references catalog.product(id),
  branch_id uuid not null,
  price_minor bigint not null
);
```

## 5. Schema: `inventory` (Module: Inventory)

```sql
create table inventory.stock_movement (
  id uuid primary key,
  product_variant_id uuid not null,
  location_id uuid not null,            -- branch_id or warehouse_id
  quantity_delta integer not null,
  reason text not null,                 -- sale | return | receipt | transfer_out | transfer_in | adjustment | count_variance
  unit_cost_minor bigint,
  batch_id uuid,
  reference_type text,
  reference_id uuid,
  created_at timestamptz not null default now(),
  created_by uuid
);
create index idx_stock_move_variant_loc on inventory.stock_movement(product_variant_id, location_id, created_at);

create table inventory.batch (
  id uuid primary key,
  product_variant_id uuid not null,
  batch_code text,
  expiry_date date,
  received_at timestamptz not null default now()
);

create table inventory.transfer_order (
  id uuid primary key,
  source_location_id uuid not null,
  destination_location_id uuid not null,
  status text not null,                 -- requested | approved | picked | in_transit | received
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table inventory.transfer_line (
  id uuid primary key,
  transfer_order_id uuid not null references inventory.transfer_order(id),
  product_variant_id uuid not null,
  quantity integer not null
);
```

## 6. Schema: `pos` (Module: POS / Sales)

```sql
create table pos.sale (
  id uuid primary key,
  branch_id uuid not null,
  customer_id uuid,
  cashier_id uuid not null,
  status text not null,                 -- open | held | completed | voided | refunded
  subtotal_minor bigint not null default 0,
  discount_minor bigint not null default 0,
  tax_minor bigint not null default 0,
  total_minor bigint not null default 0,
  currency char(3) not null,
  client_uuid uuid not null unique,     -- offline idempotency key, generated on terminal
  completed_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index idx_sale_branch_completed on pos.sale(branch_id, completed_at);

create table pos.sale_line (
  id uuid primary key,
  sale_id uuid not null references pos.sale(id),
  product_variant_id uuid not null,
  quantity numeric(12,3) not null,
  unit_price_minor bigint not null,
  discount_minor bigint not null default 0,
  tax_minor bigint not null default 0
);

create table pos.payment (
  id uuid primary key,
  sale_id uuid not null references pos.sale(id),
  method text not null,                 -- cash | card | mobile_money | gift_card | store_credit | on_account
  amount_minor bigint not null,
  reference text,
  created_at timestamptz not null default now()
);

create table pos.return_line (
  id uuid primary key,
  original_sale_line_id uuid not null references pos.sale_line(id),
  return_sale_id uuid not null references pos.sale(id),
  quantity numeric(12,3) not null,
  refund_method text not null
);
```

## 7. Schema: `purchasing` (Module: Purchasing & Suppliers)

```sql
create table purchasing.supplier (
  id uuid primary key,
  company_id uuid not null,
  name text not null,
  payment_terms text,
  lead_time_days integer
);

create table purchasing.purchase_order (
  id uuid primary key,
  supplier_id uuid not null references purchasing.supplier(id),
  branch_id uuid not null,
  status text not null,                 -- draft | approved | partially_received | received | cancelled
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table purchasing.purchase_order_line (
  id uuid primary key,
  purchase_order_id uuid not null references purchasing.purchase_order(id),
  product_variant_id uuid not null,
  quantity_ordered numeric(12,3) not null,
  quantity_received numeric(12,3) not null default 0,
  unit_cost_minor bigint not null
);
```

## 8. Schema: `customer` (Module: Customers & CRM)

```sql
create table customer.customer (
  id uuid primary key,
  company_id uuid not null,
  name text not null,
  email text,
  phone text,
  credit_limit_minor bigint not null default 0,
  loyalty_tier text,
  created_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table customer.loyalty_ledger (
  id uuid primary key,
  customer_id uuid not null references customer.customer(id),
  points_delta integer not null,
  reason text not null,                 -- accrual | redemption | expiry
  reference_id uuid,
  created_at timestamptz not null default now()
);

create table customer.store_credit_ledger (
  id uuid primary key,
  customer_id uuid not null references customer.customer(id),
  amount_delta_minor bigint not null,
  reason text not null,
  reference_id uuid,
  created_at timestamptz not null default now()
);
```

## 9. Schema: `promotion` (Module: Discount / Promotion / Loyalty Engine)

```sql
create table promotion.promotion (
  id uuid primary key,
  company_id uuid not null,
  name text not null,
  type text not null,                   -- percentage | amount | bogo | bundle | threshold
  rules jsonb not null,                 -- eligibility: date range, branch/product/segment scope
  active boolean not null default true
);

create table promotion.coupon (
  id uuid primary key,
  code text not null unique,
  promotion_id uuid references promotion.promotion(id),
  max_redemptions integer,
  redemptions_count integer not null default 0,
  expires_at timestamptz
);

create table promotion.gift_card (
  id uuid primary key,
  code text not null unique,
  balance_minor bigint not null,
  currency char(3) not null,
  issued_at timestamptz not null default now()
);
```

## 10. Schema: `accounting` (Module: Accounting)

```sql
create table accounting.account (
  id uuid primary key,
  company_id uuid not null,
  code text not null,
  name text not null,
  type text not null                    -- asset | liability | equity | revenue | expense
);

create table accounting.journal_entry (
  id uuid primary key,
  branch_id uuid not null,
  reference_type text not null,         -- sale | return | purchase_receipt | expense
  reference_id uuid not null,
  posted_at timestamptz not null default now()
);

create table accounting.journal_line (
  id uuid primary key,
  journal_entry_id uuid not null references accounting.journal_entry(id),
  account_id uuid not null references accounting.account(id),
  debit_minor bigint not null default 0,
  credit_minor bigint not null default 0,
  check (debit_minor = 0 or credit_minor = 0)
);
```
Add a database-level trigger (`V<n>__add_journal_balance_check.sql`) that rejects a `journal_entry` whose child `journal_line` rows do not sum debits = credits — this is a hard financial-integrity guarantee, not just an application-layer check.

## 11. Schema: `restaurant` (Module: Restaurant Operations — optional)

```sql
create table restaurant.dining_table (
  id uuid primary key,
  branch_id uuid not null,
  label text not null,
  status text not null default 'available' -- available | seated | ordered | needs_bussing
);

create table restaurant.menu_item (
  id uuid primary key,
  product_id uuid not null,             -- references catalog.product by UUID, no DB FK across schemas
  name text not null
);

create table restaurant.recipe_line (
  id uuid primary key,
  menu_item_id uuid not null references restaurant.menu_item(id),
  ingredient_product_variant_id uuid not null,
  quantity_per_serving numeric(12,3) not null
);
```

## 12. Migration Workflow

1. Every schema change ships as a new Flyway file under its module's folder — never edit a previously applied migration.
2. `mvn flyway:migrate` runs automatically on app startup in `local`/`test` profiles; in `staging`/`prod`, migrations run as an explicit CI/CD step before the new app version deploys.
3. Every migration is reviewed for: correct schema (module ownership), no cross-schema FK, money as `bigint`, `created_at`/`updated_at`/`deleted_at` present on transactional tables.
