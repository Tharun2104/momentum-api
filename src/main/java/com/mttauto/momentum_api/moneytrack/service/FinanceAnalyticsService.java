package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.moneytrack.dto.CategorySummaryResponse;
import com.mttauto.momentum_api.moneytrack.dto.MonthlySummaryResponse;
import com.mttauto.momentum_api.moneytrack.dto.PaymentMethodResponse;
import com.mttauto.momentum_api.moneytrack.dto.PaymentMethodSummaryResponse;
import com.mttauto.momentum_api.moneytrack.entity.Expense;
import com.mttauto.momentum_api.moneytrack.entity.ExpenseCategory;
import com.mttauto.momentum_api.moneytrack.entity.PaymentMethod;
import com.mttauto.momentum_api.moneytrack.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceAnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final PaymentMethodService paymentMethodService;

    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(YearMonth month) {
        Long userId = CurrentUserContext.get().getId();
        List<Expense> expenses = getExpensesForMonth(userId, month);
        BigDecimal totalSpent = sumExpenses(expenses);
        long transactionCount = expenses.size();
        BigDecimal average = transactionCount == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalSpent.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP);

        return new MonthlySummaryResponse(userId.toString(), month, totalSpent, transactionCount, average);
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getCategorySummary(YearMonth month) {
        Long userId = CurrentUserContext.get().getId();
        List<Expense> expenses = getExpensesForMonth(userId, month);
        BigDecimal totalSpent = sumExpenses(expenses);
        Map<ExpenseCategory, BigDecimal> totals = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return totals.entrySet().stream()
                .map(entry -> new CategorySummaryResponse(
                        entry.getKey(),
                        entry.getValue(),
                        percentage(entry.getValue(), totalSpent)
                ))
                .sorted(Comparator.comparing(CategorySummaryResponse::totalAmount).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodSummaryResponse> getPaymentMethodSummary(YearMonth month) {
        Long userId = CurrentUserContext.get().getId();
        List<Expense> expenses = getExpensesForMonth(userId, month).stream()
                .filter(expense -> expense.getPaymentMethod() != null)
                .toList();
        Map<PaymentMethod, BigDecimal> totals = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return totals.entrySet().stream()
                .map(entry -> new PaymentMethodSummaryResponse(
                        paymentMethodService.toResponse(entry.getKey()),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(PaymentMethodSummaryResponse::totalAmount).reversed())
                .toList();
    }

    private List<Expense> getExpensesForMonth(Long userId, YearMonth month) {
        Objects.requireNonNull(month, "month is required");
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        return expenseRepository.findByUser_IdAndExpenseDateBetween(userId, startDate, endDate);
    }

    private BigDecimal sumExpenses(List<Expense> expenses) {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return amount.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }
}
