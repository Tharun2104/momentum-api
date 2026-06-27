package com.mttauto.momentum_api.moneytrack.repository;

import com.mttauto.momentum_api.moneytrack.entity.SharedExpense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SharedExpenseRepository extends JpaRepository<SharedExpense, Long> {

    @EntityGraph(attributePaths = {
            "createdBy",
            "paidBy",
            "friend",
            "originalExpense",
            "participants",
            "participants.user"
    })
    @Query("""
            select distinct se
            from SharedExpense se
            join se.participants participant
            where participant.user.id = :userId
            order by se.expenseDate desc, se.createdAt desc
            """)
    List<SharedExpense> findVisibleToUser(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {
            "createdBy",
            "paidBy",
            "friend",
            "originalExpense",
            "participants",
            "participants.user"
    })
    @Query("""
            select distinct se
            from SharedExpense se
            join se.participants participant
            where se.id = :id and participant.user.id = :userId
            """)
    Optional<SharedExpense> findByIdVisibleToUser(@Param("id") Long id, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {
            "paidBy",
            "friend",
            "participants",
            "participants.user"
    })
    @Query("""
            select distinct se
            from SharedExpense se
            join se.participants participant
            where se.originalExpense.id = :expenseId and participant.user.id = :userId
            """)
    Optional<SharedExpense> findByOriginalExpenseIdVisibleToUser(
            @Param("expenseId") Long expenseId,
            @Param("userId") Long userId
    );
}
