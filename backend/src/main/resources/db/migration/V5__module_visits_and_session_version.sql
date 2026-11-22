alter table app_users add column session_version integer not null default 0;

create table learning_module_visits (
    user_id bigint not null references app_users(id) on delete cascade,
    module_key varchar(100) not null references educational_contents(content_key) on delete cascade,
    primary key (user_id, module_key)
);
