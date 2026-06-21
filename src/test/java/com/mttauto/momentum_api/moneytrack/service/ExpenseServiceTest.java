package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.moneytrack.dto.CreateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.dto.CreatePaymentMethodRequest;
import com.mttauto.momentum_api.moneytrack.dto.ExpenseResponse;
import com.mttauto.momentum_api.moneytrack.dto.UpdateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.entity.ExpenseCategory;
import com.mttauto.momentum_api.moneytrack.entity.PaymentMethodType;
import com.mttauto.momentum_api.exception.ResourceNotFoundException;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ExpenseServiceTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private ExpenseService expenseService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        CurrentUserContext.clear();
        expenseRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        userRepository.deleteAll();
        currentUser = userRepository.save(new User("User One", "user1@example.com", "password-hash"));
        CurrentUserContext.set(currentUser);
    }

    @Test
    void createExpenseSavesManualExpenseWithPaymentMethod() {
        Long paymentMethodId = createPaymentMethod("Amex");

        ExpenseResponse response = expenseService.createExpense(new CreateExpenseRequest(
                new BigDecimal("42.50"),
                ExpenseCategory.GROCERIES,
                "Walmart",
                paymentMethodId,
                LocalDate.of(2026, 6, 20),
                "Weekly groceries"
        ));

        assertThat(response.id()).isNotNull();
        assertThat(response.userId()).isEqualTo(currentUser.getId().toString());
        assertThat(response.amount()).isEqualByComparingTo("42.50");
        assertThat(response.category()).isEqualTo(ExpenseCategory.GROCERIES);
        assertThat(response.merchantName()).isEqualTo("Walmart");
        assertThat(response.paymentMethod().id()).isEqualTo(paymentMethodId);
        assertThat(response.expenseDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(response.notes()).isEqualTo("Weekly groceries");
    }

    @Test
    void createExpenseRejectsPaymentMethodOwnedByAnotherUser() {
        User otherUser = userRepository.save(new User("User Two", "user2@example.com", "password-hash"));
        CurrentUserContext.set(otherUser);
        Long paymentMethodId = paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest("Chase", PaymentMethodType.CREDIT_CARD)
        ).id();
        CurrentUserContext.set(currentUser);

        CreateExpenseRequest request = new CreateExpenseRequest(
                new BigDecimal("15.00"),
                ExpenseCategory.FOOD,
                "Lunch",
                paymentMethodId,
                LocalDate.of(2026, 6, 20),
                null
        );

        assertThatThrownBy(() -> expenseService.createExpense(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment method not found with id " + paymentMethodId);
    }

    @Test
    void getExpensesReturnsOnlyRequestedUserInNewestFirstOrder() {
        expenseService.createExpense(validExpense("Coffee", "5.25", LocalDate.of(2026, 6, 19), null));
        ExpenseResponse newest = expenseService.createExpense(validExpense("Target", "27.99", LocalDate.of(2026, 6, 20), null));
        User otherUser = userRepository.save(new User("User Two", "user2@example.com", "password-hash"));
        CurrentUserContext.set(otherUser);
        expenseService.createExpense(validExpense("Hidden", "99.99", LocalDate.of(2026, 6, 20), null));
        CurrentUserContext.set(currentUser);

        List<ExpenseResponse> responses = expenseService.getExpenses();

        assertThat(responses).extracting(ExpenseResponse::id).containsExactly(newest.id(), responses.get(1).id());
        assertThat(responses).extracting(ExpenseResponse::merchantName).containsExactly("Target", "Coffee");
    }

    @Test
    void updateExpenseChangesFields() {
        ExpenseResponse created = expenseService.createExpense(validExpense("Target", "30.00", LocalDate.of(2026, 6, 20), null));
        Long paymentMethodId = createPaymentMethod("Cash");

        ExpenseResponse updated = expenseService.updateExpense(created.id(), new UpdateExpenseRequest(
                new BigDecimal("31.25"),
                ExpenseCategory.SHOPPING,
                "Target",
                paymentMethodId,
                LocalDate.of(2026, 6, 21),
                "Household"
        ));

        assertThat(updated.amount()).isEqualByComparingTo("31.25");
        assertThat(updated.category()).isEqualTo(ExpenseCategory.SHOPPING);
        assertThat(updated.paymentMethod().nickname()).isEqualTo("Cash");
        assertThat(updated.expenseDate()).isEqualTo(LocalDate.of(2026, 6, 21));
        assertThat(updated.notes()).isEqualTo("Household");
    }

    @Test
    void deleteExpenseRemovesItForUser() {
        ExpenseResponse created = expenseService.createExpense(validExpense("Coffee", "5.25", LocalDate.of(2026, 6, 20), null));

        expenseService.deleteExpense(created.id());

        assertThatThrownBy(() -> expenseService.getExpense(created.id()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found with id " + created.id());
    }

    private Long createPaymentMethod(String nickname) {
        return paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest(nickname, PaymentMethodType.CREDIT_CARD)
        ).id();
    }

    private CreateExpenseRequest validExpense(
            String merchantName,
            String amount,
            LocalDate expenseDate,
            Long paymentMethodId
    ) {
        return new CreateExpenseRequest(
                new BigDecimal(amount),
                ExpenseCategory.FOOD,
                merchantName,
                paymentMethodId,
                expenseDate,
                null
        );
    }
}
