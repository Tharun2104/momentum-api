package com.mttauto.momentum_api.moneytrack.dto;

import java.math.BigDecimal;

public record PaymentMethodSummaryResponse(
        PaymentMethodResponse paymentMethod,
        BigDecimal totalAmount
) {
}
