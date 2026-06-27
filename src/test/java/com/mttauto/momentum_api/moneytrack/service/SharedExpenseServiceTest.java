package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.friends.entity.FriendRequest;
import com.mttauto.momentum_api.friends.repository.FriendRequestRepository;
import com.mttauto.momentum_api.moneytrack.dto.CreateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.dto.CreateExpenseSplitRequest;
import com.mttauto.momentum_api.moneytrack.entity.ExpenseCategory;
import com.mttauto.momentum_api.moneytrack.entity.SplitType;
import com.mttauto.momentum_api.moneytrack.repository.ExpenseRepository;
import com.mttauto.momentum_api.moneytrack.repository.SharedExpenseRepository;
import com.mttauto.momentum_api.user.User;
import com.mttauto.momentum_api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SharedExpenseServiceTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SharedExpenseRepository sharedExpenseRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private SharedExpenseService sharedExpenseService;

    private User payer;
    private User friend;

    @BeforeEach
    void setUp() {
        CurrentUserContext.clear();
        sharedExpenseRepository.deleteAll();
        friendRequestRepository.deleteAll();
        expenseRepository.deleteAll();
        userRepository.deleteAll();
        payer = userRepository.save(new User("Tharun", "tharun@example.com", "password-hash"));
        friend = userRepository.save(new User("Prathibha", "prathibha@example.com", "password-hash"));
        FriendRequest friendRequest = new FriendRequest(payer, friend);
        friendRequest.accept();
        friendRequestRepository.save(friendRequest);
    }

    @Test
    void getSharedExpensesReturnsSplitToPayerAndFriendOnly() {
        createDinnerSplit();

        CurrentUserContext.set(payer);
        assertThat(sharedExpenseService.getSharedExpenses()).singleElement()
                .satisfies(response -> {
                    assertThat(response.title()).isEqualTo("Dinner");
                    assertThat(response.totalAmount()).isEqualByComparingTo("100.00");
                    assertThat(response.paidByUserId()).isEqualTo(payer.getId());
                    assertThat(response.paidByName()).isEqualTo("Tharun");
                    assertThat(response.friendUserId()).isEqualTo(friend.getId());
                    assertThat(response.friendName()).isEqualTo("Prathibha");
                    assertThat(response.currentUserShareAmount()).isEqualByComparingTo("50.00");
                    assertThat(response.currentUserPaidAmount()).isEqualByComparingTo("100.00");
                    assertThat(response.currentUserNetAmount()).isEqualByComparingTo("50.00");
                    assertThat(response.otherUserName()).isEqualTo("Prathibha");
                    assertThat(response.displayText()).isEqualTo("Prathibha owes you $50");
                });

        CurrentUserContext.set(friend);
        assertThat(sharedExpenseService.getSharedExpenses()).singleElement()
                .satisfies(response -> {
                    assertThat(response.currentUserShareAmount()).isEqualByComparingTo("50.00");
                    assertThat(response.currentUserPaidAmount()).isEqualByComparingTo("0.00");
                    assertThat(response.currentUserNetAmount()).isEqualByComparingTo("-50.00");
                    assertThat(response.otherUserName()).isEqualTo("Tharun");
                    assertThat(response.displayText()).isEqualTo("You owe Tharun $50");
                });

        CurrentUserContext.set(userRepository.save(new User("Other", "other@example.com", "password-hash")));
        assertThat(sharedExpenseService.getSharedExpenses()).isEmpty();
    }

    @Test
    void deleteSharedExpenseRemovesLinkedPersonalExpense() {
        createDinnerSplit();
        CurrentUserContext.set(payer);
        Long sharedExpenseId = sharedExpenseService.getSharedExpenses().get(0).id();
        Long originalExpenseId = sharedExpenseService.getSharedExpense(sharedExpenseId).originalExpenseId();

        sharedExpenseService.deleteSharedExpense(sharedExpenseId);

        assertThat(sharedExpenseRepository.findById(sharedExpenseId)).isEmpty();
        assertThat(expenseRepository.findById(originalExpenseId)).isEmpty();
    }

    private void createDinnerSplit() {
        CurrentUserContext.set(payer);
        expenseService.createExpense(new CreateExpenseRequest(
                new BigDecimal("100.00"),
                ExpenseCategory.FOOD,
                "Dinner",
                null,
                LocalDate.of(2026, 6, 20),
                null,
                new CreateExpenseSplitRequest(true, friend.getId(), SplitType.EQUAL)
        ));
    }
}
