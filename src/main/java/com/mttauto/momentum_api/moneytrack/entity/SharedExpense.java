package com.mttauto.momentum_api.moneytrack.entity;

import com.mttauto.momentum_api.user.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shared_expenses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharedExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paid_by_user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User paidBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "friend_user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User friend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_expense_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Expense originalExpense;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private ExpenseCategory category;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false, length = 20)
    private SplitType splitType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SharedExpenseStatus status;

    @OneToMany(mappedBy = "sharedExpense", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<SharedExpenseParticipant> participants = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SharedExpense(
            User createdBy,
            User paidBy,
            User friend,
            Expense originalExpense,
            String title,
            BigDecimal totalAmount,
            ExpenseCategory category,
            LocalDate expenseDate,
            SplitType splitType
    ) {
        this.createdBy = createdBy;
        this.paidBy = paidBy;
        this.friend = friend;
        this.originalExpense = originalExpense;
        this.title = title;
        this.totalAmount = totalAmount;
        this.category = category;
        this.expenseDate = expenseDate;
        this.splitType = splitType;
        this.status = SharedExpenseStatus.ACTIVE;
    }

    public void addParticipant(SharedExpenseParticipant participant) {
        participants.add(participant);
        participant.attachTo(this);
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
}
