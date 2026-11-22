create table app_users (
 id bigserial primary key, name varchar(130) not null, email varchar(254) not null unique,
 password_hash varchar(255) not null, verified boolean not null default false,
 goal varchar(80), level varchar(80), learning_format varchar(40),
 theme varchar(10) not null default 'system', notifications boolean not null default true,
 created_at timestamp with time zone not null default now()
);
create table verification_tokens (
 id bigserial primary key, user_id bigint not null references app_users(id) on delete cascade,
 token_hash varchar(64) not null unique, purpose varchar(20) not null,
 expires_at timestamp with time zone not null, used boolean not null default false
);
create table financial_goals (
 id bigserial primary key, user_id bigint not null references app_users(id) on delete cascade,
 title varchar(150) not null, target_amount numeric(15,2) not null check (target_amount > 0),
 current_amount numeric(15,2) not null default 0 check (current_amount >= 0),
 deadline date, created_at timestamp with time zone not null default now()
);
create table budget_entries (
 id bigserial primary key, user_id bigint not null references app_users(id) on delete cascade,
 title varchar(150) not null, category varchar(80) not null, kind varchar(10) not null check(kind in ('INCOME','EXPENSE')),
 amount numeric(15,2) not null check(amount > 0), entry_date date not null
);
create table learning_progress (
 id bigserial primary key, user_id bigint not null references app_users(id) on delete cascade,
 module_key varchar(100) not null, completed_at timestamp with time zone not null default now(),
 unique(user_id,module_key)
);
create table favorites (
 id bigserial primary key, user_id bigint not null references app_users(id) on delete cascade,
 content_key varchar(100) not null, unique(user_id,content_key)
);
create index ix_goals_user on financial_goals(user_id);
create index ix_budget_user_date on budget_entries(user_id,entry_date);
