package com.mttauto.momentum_api.moneytrack.dto;

import com.mttauto.momentum_api.friends.dto.FriendUserResponse;
import com.mttauto.momentum_api.moneytrack.entity.ExpenseCategory;
import com.mttauto.momentum_api.moneytrack.entity.SharedExpenseStatus;
import com.mttauto.momentum_api.moneytrack.entity.SplitType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record SharedExpenseResponse(
        Long id,
        String title,
        BigDecimal totalAmount,
        ExpenseCategory category,
        LocalDate expenseDate,
        Long paidByUserId,
        String paidByName,
        Long friendUserId,
        String friendName,
        BigDecimal currentUserShareAmount,
        BigDecimal currentUserPaidAmount,
        BigDecimal currentUserNetAmount,
        String otherUserName,
        String displayText,
        FriendUserResponse createdBy,
        FriendUserResponse paidBy,
        FriendUserResponse friend,
        Long originalExpenseId,
        SplitType splitType,
        SharedExpenseStatus status,
        List<SharedExpenseParticipantResponse> participants,
        Instant createdAt,
        Instant updatedAt
) {
}
