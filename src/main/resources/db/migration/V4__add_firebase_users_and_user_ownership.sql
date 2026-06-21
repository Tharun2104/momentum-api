create table users (
    id bigserial primary key,
    firebase_uid varchar(128) not null unique,
    email varchar(255),
    display_name varchar(255),
    created_at timestamptz not null
);

insert into users (firebase_uid, email, display_name, created_at)
values ('local-demo-user', 'demo@momentum.local', 'Demo User', now())
on conflict (firebase_uid) do nothing;

alter table payment_methods add column owner_id bigint;
update payment_methods
set owner_id = (select id from users where firebase_uid = 'local-demo-user')
where owner_id is null;
alter table payment_methods alter column owner_id set not null;
drop index if exists idx_payment_methods_user_created_at;
alter table payment_methods drop column user_id;
alter table payment_methods rename column owner_id to user_id;
alter table payment_methods
    add constraint fk_payment_methods_user
    foreign key (user_id)
    references users (id)
    on delete cascade;

alter table expenses add column owner_id bigint;
update expenses
set owner_id = (select id from users where firebase_uid = 'local-demo-user')
where owner_id is null;
alter table expenses alter column owner_id set not null;
drop index if exists idx_expenses_user_expense_date;
drop index if exists idx_expenses_user_category;
alter table expenses drop column user_id;
alter table expenses rename column owner_id to user_id;
alter table expenses
    add constraint fk_expenses_user
    foreign key (user_id)
    references users (id)
    on delete cascade;

create index idx_users_firebase_uid on users (firebase_uid);
create index idx_payment_methods_user_created_at on payment_methods (user_id, created_at desc);
create index idx_expenses_user_expense_date on expenses (user_id, expense_date desc);
create index idx_expenses_user_category on expenses (user_id, category);
