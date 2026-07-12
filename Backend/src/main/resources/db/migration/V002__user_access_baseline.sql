create table user_access.role (
    id          uuid primary key,
    name        text not null unique,
    description text
);

create table user_access.app_user (
    id              uuid primary key,
    company_id      uuid not null,
    email           text unique,
    phone           text,
    password_hash   text,
    pin_hash        text,
    mfa_enabled     boolean not null default false,
    is_active       boolean not null default true,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    deleted_at      timestamptz
);

create table user_access.user_role_assignment (
    id          uuid primary key,
    user_id     uuid not null references user_access.app_user(id),
    role_id     uuid not null references user_access.role(id),
    branch_id   uuid,
    created_at  timestamptz not null default now()
);

create index idx_ura_user on user_access.user_role_assignment(user_id);
