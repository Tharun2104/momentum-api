package com.mttauto.momentum_api.moneytrack.dto;

import java.math.BigDecimal;

public record ExpenseSplitSummaryResponse(
        Long sharedExpenseId,
        Long friendUserId,
        String friendName,
        BigDecimal totalAmount,
        BigDecimal currentUserShareAmount,
        BigDecimal currentUserPaidAmount,
        BigDecimal currentUserNetAmount,
        String displayText
) {
}
