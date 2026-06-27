package com.mttauto.momentum_api.moneytrack.dto;

import com.mttauto.momentum_api.friends.dto.FriendUserResponse;

import java.math.BigDecimal;
import java.time.Instant;

public record SharedExpenseParticipantResponse(
        Long id,
        FriendUserResponse user,
        BigDecimal shareAmount,
        BigDecimal paidAmount,
        BigDecimal netAmount,
        Instant createdAt,
        Instant updatedAt
) {
}
