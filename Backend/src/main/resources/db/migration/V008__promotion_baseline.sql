create table promotion.promotion (
    id              uuid primary key,
    company_id      uuid not null,
    name            text not null,
    type            text not null,
    rules           jsonb not null,
    active          boolean not null default true,
    created_at      timestamptz not null default now()
);

create table promotion.coupon (
    id              uuid primary key,
    code            text not null unique,
    promotion_id    uuid references promotion.promotion(id),
    max_redemptions integer,
    redemptions_count integer not null default 0,
    expires_at      timestamptz,
    created_at      timestamptz not null default now()
);

create table promotion.gift_card (
    id              uuid primary key,
    code            text not null unique,
    balance_minor   bigint not null,
    currency        varchar(3) not null,
    issued_at       timestamptz not null default now(),
    created_at      timestamptz not null default now()
);
