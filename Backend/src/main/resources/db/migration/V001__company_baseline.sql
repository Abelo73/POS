create table company.company (
    id          uuid primary key,
    name        text not null,
    default_currency varchar(3) not null,
    billing_status   text not null default 'ACTIVE',
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now(),
    deleted_at  timestamptz
);

create table company.branch (
    id          uuid primary key,
    company_id  uuid not null references company.company(id),
    name        text not null,
    timezone    text not null,
    tax_zone    text not null,
    currency    varchar(3) not null,
    address     jsonb,
    opening_hours jsonb,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now(),
    deleted_at  timestamptz
);

create index idx_branch_company on company.branch(company_id);
