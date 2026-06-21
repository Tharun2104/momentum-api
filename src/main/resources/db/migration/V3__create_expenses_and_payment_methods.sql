create table payment_methods (
    id bigserial primary key,
    user_id varchar(120) not null,
    nickname varchar(80) not null,
    type varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table expenses (
    id bigserial primary key,
    user_id varchar(120) not null,
    amount numeric(12, 2) not null,
    category varchar(40) not null,
    merchant_name varchar(120),
    payment_method_id bigint,
    expense_date date not null,
    notes varchar(500),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_expenses_payment_method
        foreign key (payment_method_id)
        references payment_methods (id)
        on delete set null
);

create index idx_payment_methods_user_created_at on payment_methods (user_id, created_at desc);
create index idx_expenses_user_expense_date on expenses (user_id, expense_date desc);
create index idx_expenses_user_category on expenses (user_id, category);
create index idx_expenses_payment_method_id on expenses (payment_method_id);
