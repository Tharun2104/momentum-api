package com.mttauto.momentum_api.moneytrack.dto;

import com.mttauto.momentum_api.moneytrack.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        String userId,
        BigDecimal amount,
        ExpenseCategory category,
        String merchantName,
        PaymentMethodResponse paymentMethod,
        LocalDate expenseDate,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
