create table catalog.category (
    id              uuid primary key,
    company_id      uuid not null,
    parent_id       uuid references catalog.category(id),
    name            text not null,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    deleted_at      timestamptz
);

create table catalog.brand (
    id              uuid primary key,
    company_id      uuid not null,
    name            text not null,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    deleted_at      timestamptz
);

create table catalog.product (
    id              uuid primary key,
    company_id      uuid not null,
    sku             text not null,
    name            text not null,
    category_id     uuid references catalog.category(id),
    brand_id        uuid references catalog.brand(id),
    base_price_minor bigint not null,
    currency        varchar(3) not null,
    tax_class       text not null default 'STANDARD',
    track_inventory boolean not null default true,
    costing_method  text not null default 'FIFO',
    is_composite    boolean not null default false,
    sold_by_weight  boolean not null default false,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    deleted_at      timestamptz,
    unique (company_id, sku)
);

create table catalog.product_variant (
    id              uuid primary key,
    product_id      uuid not null references catalog.product(id),
    variant_name    text not null,
    barcode         text,
    price_override_minor bigint,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    deleted_at      timestamptz
);
create index idx_variant_product on catalog.product_variant(product_id);

create table catalog.branch_price_override (
    id              uuid primary key,
    product_id      uuid not null references catalog.product(id),
    branch_id       uuid not null,
    price_minor     bigint not null,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);
