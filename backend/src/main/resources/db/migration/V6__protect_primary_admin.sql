alter table app_users add column primary_admin boolean not null default false;

update app_users
set primary_admin = true
where email = 'admin@moneyhere.local' and role = 'ADMIN';
