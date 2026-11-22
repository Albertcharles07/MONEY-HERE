alter table app_users
    add column role varchar(16) not null default 'USER',
    add column enabled boolean not null default true,
    add constraint ck_app_users_role check (role in ('USER', 'ADMIN'));
