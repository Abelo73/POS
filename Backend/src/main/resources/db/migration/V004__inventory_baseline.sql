create table inventory.stock_movement (
    id                  uuid primary key,
    product_variant_id  uuid not null,
    location_id         uuid not null,
    quantity_delta      integer not null,
    reason              text not null,
    unit_cost_minor     bigint,
    batch_id            uuid,
    reference_type      text,
    reference_id        uuid,
    created_at          timestamptz not null default now(),
    created_by          uuid
);
create index idx_stock_move_variant_loc on inventory.stock_movement(product_variant_id, location_id, created_at);
