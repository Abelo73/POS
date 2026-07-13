create table purchasing.supplier (
    id              uuid primary key,
    company_id      uuid not null,
    name            text not null,
    payment_terms   text,
    lead_time_days  integer,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    deleted_at      timestamptz
);

create table purchasing.purchase_order (
    id              uuid primary key,
    supplier_id     uuid not null references purchasing.supplier(id),
    branch_id       uuid not null,
    status          text not null default 'DRAFT',
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);

create table purchasing.purchase_order_line (
    id                  uuid primary key,
    purchase_order_id   uuid not null references purchasing.purchase_order(id),
    product_variant_id  uuid not null,
    quantity_ordered    numeric(12,3) not null,
    quantity_received   numeric(12,3) not null default 0,
    unit_cost_minor     bigint not null
);
