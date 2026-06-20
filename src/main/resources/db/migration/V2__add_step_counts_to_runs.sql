alter table runs
    add column app_step_count bigint,
    add column health_kit_start_step_count bigint,
    add column health_kit_end_step_count bigint,
    add column health_kit_update_lag_seconds bigint;
