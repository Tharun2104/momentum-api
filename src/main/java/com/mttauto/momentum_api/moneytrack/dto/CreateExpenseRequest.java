package com.mttauto.momentum_api.moneytrack.dto;

import com.mttauto.momentum_api.moneytrack.entity.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "category is required")
        ExpenseCategory category,

        @Size(max = 120, message = "merchantName must be 120 characters or fewer")
        String merchantName,

        Long paymentMethodId,

        @NotNull(message = "expenseDate is required")
        LocalDate expenseDate,

        @Size(max = 500, message = "notes must be 500 characters or fewer")
        String notes
) {
}
