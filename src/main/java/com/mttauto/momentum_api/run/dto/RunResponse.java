package com.mttauto.momentum_api.run.dto;

import java.time.Instant;
import java.util.List;

public record RunResponse(
        Long id,
        Instant startTime,
        Instant endTime,
        Double distanceMeters,
        Long durationSeconds,
        Double averagePaceSecondsPerKm,
        Long appStepCount,
        Long healthKitStartStepCount,
        Long healthKitEndStepCount,
        Long healthKitStepCount,
        Long healthKitUpdateLagSeconds,
        Instant createdAt,
        Instant updatedAt,
        List<RoutePointResponse> routePoints
) {
}
