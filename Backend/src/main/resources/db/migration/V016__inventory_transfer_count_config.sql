create table inventory.reorder_config (
    id                  uuid primary key,
    product_variant_id  uuid not null,
    location_id         uuid not null,
    reorder_level       integer not null,
    reorder_quantity    integer not null,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);

create table inventory.transfer_order (
    id                  uuid primary key,
    source_location_id  uuid not null,
    destination_location_id uuid not null,
    status              text not null default 'REQUESTED',
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);

create table inventory.transfer_line (
    id                  uuid primary key,
    transfer_order_id   uuid not null references inventory.transfer_order(id),
    product_variant_id  uuid not null,
    quantity            integer not null,
    created_at          timestamptz not null default now()
);

create table inventory.stock_count (
    id                  uuid primary key,
    location_id         uuid not null,
    status              text not null default 'DRAFT',
    variance_threshold  bigint not null default 0,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);

create table inventory.stock_count_line (
    id                  uuid primary key,
    stock_count_id      uuid not null references inventory.stock_count(id),
    product_variant_id  uuid not null,
    expected_quantity   integer not null,
    counted_quantity    integer,
    created_at          timestamptz not null default now()
);

create table inventory.branch_inventory_config (
    id                  uuid primary key,
    branch_id           uuid not null,
    allow_negative_stock boolean not null default false,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);
