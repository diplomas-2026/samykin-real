create table users (
    id bigserial primary key,
    email varchar(160) not null unique,
    password_hash varchar(255) not null,
    full_name varchar(120) not null,
    department varchar(80) not null,
    position varchar(80) not null,
    employee_code varchar(40) not null unique,
    role varchar(32) not null,
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table payouts (
    id bigserial primary key,
    payout_code varchar(40) not null unique,
    employee_id bigint not null references users(id),
    created_by_id bigint not null references users(id),
    payout_type varchar(80) not null,
    amount numeric(12, 2) not null,
    payout_date date not null,
    status varchar(32) not null,
    basis varchar(300) not null,
    comment varchar(1000) not null,
    payout_note varchar(1000),
    prepared_at timestamptz,
    paid_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table ai_settings (
    id bigint primary key,
    style_name varchar(80) not null,
    style_instruction varchar(500) not null,
    updated_at timestamptz not null
);

create table ai_usage_daily (
    usage_date date primary key,
    used_tokens integer not null
);

create table audit_logs (
    id bigserial primary key,
    actor_email varchar(160) not null,
    action varchar(160) not null,
    details varchar(500) not null,
    created_at timestamptz not null
);
