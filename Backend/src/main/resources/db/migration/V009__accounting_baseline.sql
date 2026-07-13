create table accounting.account (
    id              uuid primary key,
    company_id      uuid not null,
    code            text not null,
    name            text not null,
    type            text not null,
    created_at      timestamptz not null default now()
);

create table accounting.journal_entry (
    id              uuid primary key,
    branch_id       uuid not null,
    reference_type  text not null,
    reference_id    uuid not null,
    posted_at       timestamptz not null default now(),
    created_at      timestamptz not null default now()
);

create table accounting.journal_line (
    id              uuid primary key,
    journal_entry_id uuid not null references accounting.journal_entry(id),
    account_id      uuid not null references accounting.account(id),
    debit_minor     bigint not null default 0,
    credit_minor    bigint not null default 0,
    check (debit_minor = 0 or credit_minor = 0)
);
