create table shared_expenses (
    id bigserial primary key,
    created_by_user_id bigint not null,
    paid_by_user_id bigint not null,
    friend_user_id bigint not null,
    original_expense_id bigint,
    title varchar(120) not null,
    total_amount numeric(12, 2) not null,
    category varchar(40) not null,
    expense_date date not null,
    split_type varchar(20) not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_shared_expenses_created_by
        foreign key (created_by_user_id)
        references users (id)
        on delete cascade,
    constraint fk_shared_expenses_paid_by
        foreign key (paid_by_user_id)
        references users (id)
        on delete cascade,
    constraint fk_shared_expenses_friend
        foreign key (friend_user_id)
        references users (id)
        on delete cascade,
    constraint fk_shared_expenses_original_expense
        foreign key (original_expense_id)
        references expenses (id)
        on delete set null
);

create table shared_expense_participants (
    id bigserial primary key,
    shared_expense_id bigint not null,
    user_id bigint not null,
    share_amount numeric(12, 2) not null,
    paid_amount numeric(12, 2) not null,
    net_amount numeric(12, 2) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_shared_expense_participants_shared_expense
        foreign key (shared_expense_id)
        references shared_expenses (id)
        on delete cascade,
    constraint fk_shared_expense_participants_user
        foreign key (user_id)
        references users (id)
        on delete cascade
);

create index idx_shared_expenses_created_by on shared_expenses (created_by_user_id, created_at desc);
create index idx_shared_expenses_paid_by on shared_expenses (paid_by_user_id, created_at desc);
create index idx_shared_expenses_friend on shared_expenses (friend_user_id, created_at desc);
create index idx_shared_expense_participants_user on shared_expense_participants (user_id);
create unique index idx_shared_expense_participants_unique_user
    on shared_expense_participants (shared_expense_id, user_id);
