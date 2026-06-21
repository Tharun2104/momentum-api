alter table users
    rename column display_name to name;

alter table users
    add column password_hash varchar(255),
    add column updated_at timestamptz;

update users
set name = coalesce(nullif(trim(name), ''), 'Demo User'),
    email = coalesce(nullif(trim(email), ''), 'user-' || id || '@momentum.local'),
    password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    updated_at = created_at
where password_hash is null;

drop index if exists idx_users_firebase_uid;

alter table users
    alter column name set not null,
    alter column email set not null,
    alter column password_hash set not null,
    alter column updated_at set not null;

alter table users
    drop constraint if exists users_firebase_uid_key;

alter table users
    drop column firebase_uid;

create unique index idx_users_email on users (email);
