create table friend_requests (
    id bigserial primary key,
    sender_user_id bigint not null,
    receiver_user_id bigint not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint fk_friend_requests_sender
        foreign key (sender_user_id)
        references users (id)
        on delete cascade,
    constraint fk_friend_requests_receiver
        foreign key (receiver_user_id)
        references users (id)
        on delete cascade,
    constraint chk_friend_requests_not_self
        check (sender_user_id <> receiver_user_id)
);

create index idx_friend_requests_sender_status on friend_requests (sender_user_id, status);
create index idx_friend_requests_receiver_status on friend_requests (receiver_user_id, status);
create index idx_friend_requests_users_status on friend_requests (sender_user_id, receiver_user_id, status);
