create table customer.customer (
    id              uuid primary key,
    company_id      uuid not null,
    name            text not null,
    email           text,
    phone           text,
    credit_limit_minor bigint not null default 0,
    loyalty_tier    text,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    deleted_at      timestamptz
);

create table customer.loyalty_ledger (
    id              uuid primary key,
    customer_id     uuid not null references customer.customer(id),
    points_delta    integer not null,
    reason          text not null,
    reference_id    uuid,
    created_at      timestamptz not null default now()
);

create table customer.store_credit_ledger (
    id              uuid primary key,
    customer_id     uuid not null references customer.customer(id),
    amount_delta_minor bigint not null,
    reason          text not null,
    reference_id    uuid,
    created_at      timestamptz not null default now()
);
