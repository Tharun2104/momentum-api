package com.mttauto.momentum_api.moneytrack.dto;

import java.math.BigDecimal;

public record FriendBalanceResponse(
        Long friendUserId,
        String friendName,
        BigDecimal netBalance,
        String displayText
) {
}
