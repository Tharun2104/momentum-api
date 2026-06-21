package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.moneytrack.dto.CreateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.dto.CreatePaymentMethodRequest;
import com.mttauto.momentum_api.moneytrack.dto.MonthlySummaryResponse;
import com.mttauto.momentum_api.moneytrack.entity.ExpenseCategory;
import com.mttauto.momentum_api.moneytrack.entity.PaymentMethodType;
import com.mttauto.momentum_api.moneytrack.repository.ExpenseRepository;
import com.mttauto.momentum_api.moneytrack.repository.PaymentMethodRepository;
import com.mttauto.momentum_api.user.User;
import com.mttauto.momentum_api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FinanceAnalyticsServiceTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private FinanceAnalyticsService financeAnalyticsService;

    @BeforeEach
    void setUp() {
        CurrentUserContext.clear();
        expenseRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        userRepository.deleteAll();
        CurrentUserContext.set(userRepository.save(new User("User One", "user1@example.com", "password-hash")));
    }

    @Test
    void getMonthlySummaryCalculatesTotalsForSelectedMonth() {
        expenseService.createExpense(expense("20.00", ExpenseCategory.FOOD, LocalDate.of(2026, 6, 1), null));
        expenseService.createExpense(expense("40.00", ExpenseCategory.GROCERIES, LocalDate.of(2026, 6, 2), null));
        expenseService.createExpense(expense("100.00", ExpenseCategory.FOOD, LocalDate.of(2026, 5, 31), null));

        MonthlySummaryResponse response = financeAnalyticsService.getMonthlySummary(YearMonth.of(2026, 6));

        assertThat(response.totalSpent()).isEqualByComparingTo("60.00");
        assertThat(response.transactionCount()).isEqualTo(2);
        assertThat(response.averageTransactionAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    void getCategorySummaryCalculatesPercentagesForSelectedMonth() {
        expenseService.createExpense(expense("25.00", ExpenseCategory.FOOD, LocalDate.of(2026, 6, 1), null));
        expenseService.createExpense(expense("75.00", ExpenseCategory.GROCERIES, LocalDate.of(2026, 6, 2), null));

        var response = financeAnalyticsService.getCategorySummary(YearMonth.of(2026, 6));

        assertThat(response).hasSize(2);
        assertThat(response.get(0).category()).isEqualTo(ExpenseCategory.GROCERIES);
        assertThat(response.get(0).totalAmount()).isEqualByComparingTo("75.00");
        assertThat(response.get(0).percentage()).isEqualByComparingTo("75.00");
        assertThat(response.get(1).category()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(response.get(1).percentage()).isEqualByComparingTo("25.00");
    }

    @Test
    void getPaymentMethodSummaryCalculatesTotalsForSelectedMonth() {
        Long amexId = paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest("Amex", PaymentMethodType.CREDIT_CARD)
        ).id();
        Long cashId = paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest("Cash", PaymentMethodType.CASH)
        ).id();
        expenseService.createExpense(expense("15.00", ExpenseCategory.FOOD, LocalDate.of(2026, 6, 1), amexId));
        expenseService.createExpense(expense("35.00", ExpenseCategory.SHOPPING, LocalDate.of(2026, 6, 2), cashId));

        var response = financeAnalyticsService.getPaymentMethodSummary(YearMonth.of(2026, 6));

        assertThat(response).hasSize(2);
        assertThat(response).extracting(summary -> summary.paymentMethod().nickname()).containsExactly("Cash", "Amex");
        assertThat(response).extracting("totalAmount").containsExactly(new BigDecimal("35.00"), new BigDecimal("15.00"));
    }

    private CreateExpenseRequest expense(
            String amount,
            ExpenseCategory category,
            LocalDate expenseDate,
            Long paymentMethodId
    ) {
        return new CreateExpenseRequest(
                new BigDecimal(amount),
                category,
                "Merchant",
                paymentMethodId,
                expenseDate,
                null
        );
    }
}
