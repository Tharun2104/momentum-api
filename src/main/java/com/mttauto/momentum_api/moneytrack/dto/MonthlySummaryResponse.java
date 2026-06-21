package com.mttauto.momentum_api.moneytrack.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySummaryResponse(
        String userId,
        YearMonth month,
        BigDecimal totalSpent,
        long transactionCount,
        BigDecimal averageTransactionAmount
) {
}
