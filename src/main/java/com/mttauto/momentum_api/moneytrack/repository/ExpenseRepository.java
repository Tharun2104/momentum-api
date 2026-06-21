package com.mttauto.momentum_api.moneytrack.repository;

import com.mttauto.momentum_api.moneytrack.entity.Expense;
import com.mttauto.momentum_api.moneytrack.entity.PaymentMethod;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @EntityGraph(attributePaths = "paymentMethod")
    List<Expense> findByUser_IdOrderByExpenseDateDescCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "paymentMethod")
    Optional<Expense> findByIdAndUser_Id(Long id, Long userId);

    @EntityGraph(attributePaths = "paymentMethod")
    List<Expense> findByUser_IdAndExpenseDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = "paymentMethod")
    List<Expense> findByUser_IdAndExpenseDateBetweenAndPaymentMethodIdOrderByExpenseDateDescCreatedAtDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Long paymentMethodId
    );

    @EntityGraph(attributePaths = "paymentMethod")
    List<Expense> findByUser_IdAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Expense> findByPaymentMethod(PaymentMethod paymentMethod);
}
