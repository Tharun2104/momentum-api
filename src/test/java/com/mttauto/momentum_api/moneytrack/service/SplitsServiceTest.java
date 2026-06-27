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
class SplitsServiceTest {

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
    private SplitsService splitsService;

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
        createDinnerSplit();
    }

    @Test
    void getSummaryUsesCurrentUsersParticipantNetAmounts() {
        CurrentUserContext.set(payer);
        var payerSummary = splitsService.getSummary();

        assertThat(payerSummary.netBalance()).isEqualByComparingTo("50.00");
        assertThat(payerSummary.totalOwedToYou()).isEqualByComparingTo("50.00");
        assertThat(payerSummary.totalYouOwe()).isEqualByComparingTo("0.00");

        CurrentUserContext.set(friend);
        var friendSummary = splitsService.getSummary();

        assertThat(friendSummary.netBalance()).isEqualByComparingTo("-50.00");
        assertThat(friendSummary.totalOwedToYou()).isEqualByComparingTo("0.00");
        assertThat(friendSummary.totalYouOwe()).isEqualByComparingTo("50.00");
    }

    @Test
    void getFriendBalancesGroupsParticipantNetAmountsByOtherUser() {
        CurrentUserContext.set(payer);
        assertThat(splitsService.getFriendBalances()).singleElement()
                .satisfies(balance -> {
                    assertThat(balance.friendUserId()).isEqualTo(friend.getId());
                    assertThat(balance.friendName()).isEqualTo("Prathibha");
                    assertThat(balance.netBalance()).isEqualByComparingTo("50.00");
                    assertThat(balance.displayText()).isEqualTo("Prathibha owes you $50");
                });

        CurrentUserContext.set(friend);
        assertThat(splitsService.getFriendBalances()).singleElement()
                .satisfies(balance -> {
                    assertThat(balance.friendUserId()).isEqualTo(payer.getId());
                    assertThat(balance.friendName()).isEqualTo("Tharun");
                    assertThat(balance.netBalance()).isEqualByComparingTo("-50.00");
                    assertThat(balance.displayText()).isEqualTo("You owe Tharun $50");
                });
    }

    @Test
    void getRecentReturnsCurrentUsersSharedExpenses() {
        CurrentUserContext.set(friend);

        assertThat(splitsService.getRecent()).singleElement()
                .satisfies(response -> {
                    assertThat(response.title()).isEqualTo("Dinner");
                    assertThat(response.totalAmount()).isEqualByComparingTo("100.00");
                    assertThat(response.currentUserShareAmount()).isEqualByComparingTo("50.00");
                    assertThat(response.paidByName()).isEqualTo("Tharun");
                });
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
