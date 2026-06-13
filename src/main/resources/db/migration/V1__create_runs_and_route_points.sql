create table runs (
    id bigserial primary key,
    start_time timestamptz not null,
    end_time timestamptz not null,
    distance_meters double precision not null,
    duration_seconds bigint not null,
    average_pace_seconds_per_km double precision not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table route_points (
    id bigserial primary key,
    run_id bigint not null,
    latitude double precision not null,
    longitude double precision not null,
    recorded_at timestamptz not null,
    accuracy_meters double precision,
    sequence_number integer not null,
    created_at timestamptz not null,
    constraint fk_route_points_run
        foreign key (run_id)
        references runs (id)
        on delete cascade
);

create index idx_route_points_run_id on route_points (run_id);
create index idx_route_points_run_id_sequence_number on route_points (run_id, sequence_number);
create index idx_runs_start_time on runs (start_time);
