create table inventory.cost_layer (
    id                  uuid primary key,
    product_variant_id  uuid not null,
    location_id         uuid not null,
    quantity            integer not null,
    unit_cost_minor     bigint not null,
    created_at          timestamptz not null default now()
);
create index idx_cost_layer_variant_loc on inventory.cost_layer(product_variant_id, location_id, created_at);

create table inventory.batch (
    id                  uuid primary key,
    product_variant_id  uuid not null,
    batch_code          text,
    expiry_date         date,
    received_at         timestamptz not null default now(),
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);
