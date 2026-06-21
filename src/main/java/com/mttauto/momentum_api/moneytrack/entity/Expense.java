package com.mttauto.momentum_api.moneytrack.entity;

import com.mttauto.momentum_api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private ExpenseCategory category;

    @Column(name = "merchant_name", length = 120)
    private String merchantName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Expense(
            User user,
            BigDecimal amount,
            ExpenseCategory category,
            String merchantName,
            PaymentMethod paymentMethod,
            LocalDate expenseDate,
            String notes
    ) {
        this.user = user;
        this.amount = amount;
        this.category = category;
        this.merchantName = merchantName;
        this.paymentMethod = paymentMethod;
        this.expenseDate = expenseDate;
        this.notes = notes;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void update(
            BigDecimal amount,
            ExpenseCategory category,
            String merchantName,
            PaymentMethod paymentMethod,
            LocalDate expenseDate,
            String notes
    ) {
        this.amount = amount;
        this.category = category;
        this.merchantName = merchantName;
        this.paymentMethod = paymentMethod;
        this.expenseDate = expenseDate;
        this.notes = notes;
    }

    public void clearPaymentMethod() {
        paymentMethod = null;
    }

    public String getUserId() {
        return user.getId().toString();
    }
}
