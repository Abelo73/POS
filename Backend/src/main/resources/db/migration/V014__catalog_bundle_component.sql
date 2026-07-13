create table catalog.bundle_component (
    id              uuid primary key,
    bundle_product_id uuid not null references catalog.product(id),
    component_product_id uuid not null references catalog.product(id),
    quantity        numeric(12,3) not null,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);
create index idx_bundle_component_bundle on catalog.bundle_component(bundle_product_id);
