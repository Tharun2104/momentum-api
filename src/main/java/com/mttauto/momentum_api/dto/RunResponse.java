package com.mttauto.momentum_api.dto;

import java.time.Instant;
import java.util.List;

public record RunResponse(
        Long id,
        Instant startTime,
        Instant endTime,
        Double distanceMeters,
        Long durationSeconds,
        Double averagePaceSecondsPerKm,
        Instant createdAt,
        Instant updatedAt,
        List<RoutePointResponse> routePoints
) {
}
