package com.mttauto.momentum_api.moneytrack.dto;

import com.mttauto.momentum_api.moneytrack.entity.ExpenseCategory;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        ExpenseCategory category,
        BigDecimal totalAmount,
        BigDecimal percentage
) {
}
