package com.mttauto.momentum_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.List;

public record CreateRunRequest(
        @NotNull(message = "startTime is required")
        Instant startTime,

        @NotNull(message = "endTime is required")
        Instant endTime,

        @NotNull(message = "distanceMeters is required")
        @DecimalMin(value = "0.0", message = "distanceMeters must be greater than or equal to 0")
        Double distanceMeters,

        @NotNull(message = "durationSeconds is required")
        @Positive(message = "durationSeconds must be greater than 0")
        Long durationSeconds,

        @NotNull(message = "averagePaceSecondsPerKm is required")
        @DecimalMin(value = "0.0", message = "averagePaceSecondsPerKm must be greater than or equal to 0")
        Double averagePaceSecondsPerKm,

        @PositiveOrZero(message = "appStepCount must be greater than or equal to 0")
        Long appStepCount,

        @PositiveOrZero(message = "healthKitStartStepCount must be greater than or equal to 0")
        Long healthKitStartStepCount,

        @PositiveOrZero(message = "healthKitEndStepCount must be greater than or equal to 0")
        Long healthKitEndStepCount,

        @PositiveOrZero(message = "healthKitUpdateLagSeconds must be greater than or equal to 0")
        Long healthKitUpdateLagSeconds,

        @NotNull(message = "routePoints is required")
        List<@Valid @NotNull(message = "route point is required") CreateRoutePointRequest> routePoints
) {
}
