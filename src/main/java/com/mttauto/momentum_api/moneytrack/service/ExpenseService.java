package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.moneytrack.dto.CreateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.dto.ExpenseResponse;
import com.mttauto.momentum_api.moneytrack.dto.UpdateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.entity.Expense;
import com.mttauto.momentum_api.moneytrack.entity.PaymentMethod;
import com.mttauto.momentum_api.exception.ResourceNotFoundException;
import com.mttauto.momentum_api.moneytrack.repository.ExpenseRepository;
import com.mttauto.momentum_api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PaymentMethodService paymentMethodService;

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        User user = CurrentUserContext.get();
        PaymentMethod paymentMethod = resolvePaymentMethod(request.paymentMethodId(), user.getId());
        Expense expense = new Expense(
                user,
                request.amount(),
                request.category(),
                cleanOptionalText(request.merchantName()),
                paymentMethod,
                request.expenseDate(),
                cleanOptionalText(request.notes())
        );

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses() {
        return expenseRepository.findByUser_IdOrderByExpenseDateDescCreatedAtDesc(CurrentUserContext.get().getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(YearMonth month, Long paymentMethodId) {
        Long userId = CurrentUserContext.get().getId();
        if (month == null && paymentMethodId == null) {
            return getExpenses();
        }

        YearMonth selectedMonth = month == null ? YearMonth.now() : month;
        LocalDate startDate = selectedMonth.atDay(1);
        LocalDate endDate = selectedMonth.atEndOfMonth();

        if (paymentMethodId != null) {
            paymentMethodService.findPaymentMethod(paymentMethodId, userId);
            return expenseRepository.findByUser_IdAndExpenseDateBetweenAndPaymentMethodIdOrderByExpenseDateDescCreatedAtDesc(
                            userId,
                            startDate,
                            endDate,
                            paymentMethodId
                    ).stream()
                    .map(this::toResponse)
                    .toList();
        }

        return expenseRepository.findByUser_IdAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
                        userId,
                        startDate,
                        endDate
                ).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(Long id) {
        return toResponse(findExpense(id, CurrentUserContext.get().getId()));
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request) {
        Long userId = CurrentUserContext.get().getId();
        Expense expense = findExpense(id, userId);
        PaymentMethod paymentMethod = resolvePaymentMethod(request.paymentMethodId(), userId);
        expense.update(
                request.amount(),
                request.category(),
                cleanOptionalText(request.merchantName()),
                paymentMethod,
                request.expenseDate(),
                cleanOptionalText(request.notes())
        );

        return toResponse(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.delete(findExpense(id, CurrentUserContext.get().getId()));
    }

    private Expense findExpense(Long id, Long userId) {
        return expenseRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + id));
    }

    private PaymentMethod resolvePaymentMethod(Long paymentMethodId, Long userId) {
        if (paymentMethodId == null) {
            return null;
        }

        return paymentMethodService.findPaymentMethod(paymentMethodId, userId);
    }

    private String cleanOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getUserId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getMerchantName(),
                expense.getPaymentMethod() == null ? null : paymentMethodService.toResponse(expense.getPaymentMethod()),
                expense.getExpenseDate(),
                expense.getNotes(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
