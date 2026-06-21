package com.mttauto.momentum_api.run.dto;

import java.time.Instant;

public record RoutePointResponse(
        Long id,
        Double latitude,
        Double longitude,
        Instant recordedAt,
        Double accuracyMeters,
        Integer sequenceNumber
) {
}
