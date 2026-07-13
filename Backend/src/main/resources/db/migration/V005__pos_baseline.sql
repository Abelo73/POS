create table pos.sale (
    id              uuid primary key,
    branch_id       uuid not null,
    customer_id     uuid,
    cashier_id      uuid not null,
    status          text not null,
    subtotal_minor  bigint not null default 0,
    discount_minor  bigint not null default 0,
    tax_minor       bigint not null default 0,
    total_minor     bigint not null default 0,
    currency        varchar(3) not null,
    client_uuid     uuid not null unique,
    completed_at    timestamptz,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);
create index idx_sale_branch_completed on pos.sale(branch_id, completed_at);
create index idx_sale_client_uuid on pos.sale(client_uuid);

create table pos.sale_line (
    id              uuid primary key,
    sale_id         uuid not null references pos.sale(id),
    product_variant_id uuid not null,
    quantity        numeric(12,3) not null,
    unit_price_minor bigint not null,
    discount_minor  bigint not null default 0,
    tax_minor       bigint not null default 0
);

create table pos.payment (
    id              uuid primary key,
    sale_id         uuid not null references pos.sale(id),
    method          text not null,
    amount_minor    bigint not null,
    reference       text,
    created_at      timestamptz not null default now()
);

create table pos.return_line (
    id              uuid primary key,
    original_sale_line_id uuid not null references pos.sale_line(id),
    return_sale_id  uuid not null references pos.sale(id),
    quantity        numeric(12,3) not null,
    refund_method   text not null,
    created_at      timestamptz not null default now()
);
