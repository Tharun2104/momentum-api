package com.mttauto.momentum_api.moneytrack.dto;

import com.mttauto.momentum_api.moneytrack.entity.SplitType;

import java.util.List;

public record CreateExpenseSplitRequest(
        boolean enabled,
        Long friendUserId,
        List<Long> friendUserIds,
        SplitType splitType
) {
    public CreateExpenseSplitRequest(boolean enabled, Long friendUserId, SplitType splitType) {
        this(enabled, friendUserId, null, splitType);
    }
}
