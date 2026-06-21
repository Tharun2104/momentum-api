package com.mttauto.momentum_api.moneytrack.dto;

import com.mttauto.momentum_api.moneytrack.entity.PaymentMethodType;

import java.time.Instant;

public record PaymentMethodResponse(
        Long id,
        String userId,
        String nickname,
        PaymentMethodType type,
        Instant createdAt,
        Instant updatedAt
) {
}
