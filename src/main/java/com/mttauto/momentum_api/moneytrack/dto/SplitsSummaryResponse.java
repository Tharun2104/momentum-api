package com.mttauto.momentum_api.moneytrack.dto;

import java.math.BigDecimal;

public record SplitsSummaryResponse(
        BigDecimal netBalance,
        BigDecimal totalOwedToYou,
        BigDecimal totalYouOwe
) {
}
