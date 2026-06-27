package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.friends.entity.FriendRequest;
import com.mttauto.momentum_api.friends.repository.FriendRequestRepository;
import com.mttauto.momentum_api.moneytrack.dto.CreateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.dto.CreateExpenseSplitRequest;
import com.mttauto.momentum_api.moneytrack.dto.CreatePaymentMethodRequest;
import com.mttauto.momentum_api.moneytrack.dto.ExpenseResponse;
import com.mttauto.momentum_api.moneytrack.dto.UpdateExpenseRequest;
import com.mttauto.momentum_api.moneytrack.entity.ExpenseCategory;
import com.mttauto.momentum_api.moneytrack.entity.PaymentMethodType;
import com.mttauto.momentum_api.moneytrack.entity.SplitType;
import com.mttauto.momentum_api.exception.ResourceNotFoundException;
import com.mttauto.momentum_api.moneytrack.repository.ExpenseRepository;
import com.mttauto.momentum_api.moneytrack.repository.PaymentMethodRepository;
import com.mttauto.momentum_api.moneytrack.repository.SharedExpenseRepository;
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
    private SharedExpenseRepository sharedExpenseRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

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
        sharedExpenseRepository.deleteAll();
        friendRequestRepository.deleteAll();
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
                "Weekly groceries",
                null
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
                null,
                null
        );

        assertThatThrownBy(() -> expenseService.createExpense(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment method not found with id " + paymentMethodId);
    }

    @Test
    void createSplitExpenseSavesOnlyCurrentUsersHalfAsPersonalExpense() {
        User friend = createAcceptedFriend("Friend One", "friend@example.com");

        ExpenseResponse response = expenseService.createExpense(new CreateExpenseRequest(
                new BigDecimal("100.00"),
                ExpenseCategory.FOOD,
                "Dinner",
                createPaymentMethod("Visa"),
                LocalDate.of(2026, 6, 20),
                null,
                new CreateExpenseSplitRequest(true, friend.getId(), SplitType.EQUAL)
        ));

        assertThat(response.amount()).isEqualByComparingTo("50.00");
        assertThat(expenseRepository.findByUser_IdOrderByExpenseDateDescCreatedAtDesc(currentUser.getId()))
                .singleElement()
                .extracting("amount")
                .isEqualTo(new BigDecimal("50.00"));
        assertThat(expenseRepository.findByUser_IdOrderByExpenseDateDescCreatedAtDesc(friend.getId())).isEmpty();
    }

    @Test
    void createSplitExpenseCreatesSharedExpenseWithPayerAndFriendParticipants() {
        User friend = createAcceptedFriend("Friend One", "friend@example.com");

        expenseService.createExpense(new CreateExpenseRequest(
                new BigDecimal("100.00"),
                ExpenseCategory.FOOD,
                "Dinner",
                createPaymentMethod("Visa"),
                LocalDate.of(2026, 6, 20),
                null,
                new CreateExpenseSplitRequest(true, friend.getId(), SplitType.EQUAL)
        ));

        var sharedExpense = sharedExpenseRepository.findVisibleToUser(currentUser.getId()).get(0);

        assertThat(sharedExpense.getTotalAmount()).isEqualByComparingTo("100.00");
        assertThat(sharedExpense.getOriginalExpense().getId()).isNotNull();
        assertThat(expenseRepository.findByIdAndUser_Id(sharedExpense.getOriginalExpense().getId(), currentUser.getId()))
                .get()
                .extracting("amount")
                .isEqualTo(new BigDecimal("50.00"));
        assertThat(sharedExpense.getParticipants())
                .filteredOn(participant -> participant.getUser().getId().equals(currentUser.getId()))
                .singleElement()
                .satisfies(participant -> {
                    assertThat(participant.getShareAmount()).isEqualByComparingTo("50.00");
                    assertThat(participant.getPaidAmount()).isEqualByComparingTo("100.00");
                    assertThat(participant.getNetAmount()).isEqualByComparingTo("50.00");
                });
        assertThat(sharedExpense.getParticipants())
                .filteredOn(participant -> participant.getUser().getId().equals(friend.getId()))
                .singleElement()
                .satisfies(participant -> {
                    assertThat(participant.getShareAmount()).isEqualByComparingTo("50.00");
                    assertThat(participant.getPaidAmount()).isEqualByComparingTo("0.00");
                    assertThat(participant.getNetAmount()).isEqualByComparingTo("-50.00");
                });
    }

    @Test
    void createSplitExpenseCanSplitEquallyAcrossMultipleFriends() {
        User friendOne = createAcceptedFriend("Friend One", "friend1@example.com");
        User friendTwo = createAcceptedFriend("Friend Two", "friend2@example.com");
        User friendThree = createAcceptedFriend("Friend Three", "friend3@example.com");

        ExpenseResponse response = expenseService.createExpense(new CreateExpenseRequest(
                new BigDecimal("120.00"),
                ExpenseCategory.FOOD,
                "Group Dinner",
                createPaymentMethod("Visa"),
                LocalDate.of(2026, 6, 20),
                null,
                new CreateExpenseSplitRequest(
                        true,
                        null,
                        List.of(friendOne.getId(), friendTwo.getId(), friendThree.getId()),
                        SplitType.EQUAL
                )
        ));

        assertThat(response.amount()).isEqualByComparingTo("30.00");
        var sharedExpense = sharedExpenseRepository.findVisibleToUser(currentUser.getId()).get(0);
        assertThat(sharedExpense.getParticipants()).hasSize(4);
        assertThat(sharedExpense.getParticipants())
                .filteredOn(participant -> participant.getUser().getId().equals(currentUser.getId()))
                .singleElement()
                .satisfies(participant -> {
                    assertThat(participant.getShareAmount()).isEqualByComparingTo("30.00");
                    assertThat(participant.getPaidAmount()).isEqualByComparingTo("120.00");
                    assertThat(participant.getNetAmount()).isEqualByComparingTo("90.00");
                });
        assertThat(sharedExpense.getParticipants())
                .filteredOn(participant -> !participant.getUser().getId().equals(currentUser.getId()))
                .allSatisfy(participant -> {
                    assertThat(participant.getShareAmount()).isEqualByComparingTo("30.00");
                    assertThat(participant.getPaidAmount()).isEqualByComparingTo("0.00");
                    assertThat(participant.getNetAmount()).isEqualByComparingTo("-30.00");
                });
    }

    @Test
    void getExpenseIncludesSplitSummaryWhenExpenseCameFromSplit() {
        User friend = createAcceptedFriend("Prathibha", "prathibha@example.com");
        ExpenseResponse created = expenseService.createExpense(new CreateExpenseRequest(
                new BigDecimal("100.00"),
                ExpenseCategory.FOOD,
                "Dinner",
                createPaymentMethod("Visa"),
                LocalDate.of(2026, 6, 20),
                "Birthday",
                new CreateExpenseSplitRequest(true, friend.getId(), SplitType.EQUAL)
        ));

        ExpenseResponse response = expenseService.getExpense(created.id());

        assertThat(response.amount()).isEqualByComparingTo("50.00");
        assertThat(response.split()).isNotNull();
        assertThat(response.split().friendName()).isEqualTo("Prathibha");
        assertThat(response.split().totalAmount()).isEqualByComparingTo("100.00");
        assertThat(response.split().currentUserShareAmount()).isEqualByComparingTo("50.00");
        assertThat(response.split().displayText()).isEqualTo("Prathibha owes you $50");
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

    private User createAcceptedFriend(String name, String email) {
        User friend = userRepository.save(new User(name, email, "password-hash"));
        FriendRequest friendRequest = new FriendRequest(currentUser, friend);
        friendRequest.accept();
        friendRequestRepository.save(friendRequest);
        return friend;
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
                null,
                null
        );
    }
}
