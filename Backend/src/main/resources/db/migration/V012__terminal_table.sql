create table user_access.terminal (
    id                  uuid primary key,
    branch_id           uuid not null,
    device_fingerprint  text,
    registered_at       timestamptz not null default now()
);
