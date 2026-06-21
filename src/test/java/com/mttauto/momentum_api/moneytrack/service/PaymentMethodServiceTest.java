package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.moneytrack.dto.CreatePaymentMethodRequest;
import com.mttauto.momentum_api.moneytrack.dto.PaymentMethodResponse;
import com.mttauto.momentum_api.moneytrack.dto.UpdatePaymentMethodRequest;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PaymentMethodServiceTest {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentMethodService paymentMethodService;

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
    void createPaymentMethodSavesLabelForUser() {
        PaymentMethodResponse response = paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest("Amex", PaymentMethodType.CREDIT_CARD)
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.userId()).isEqualTo(currentUser.getId().toString());
        assertThat(response.nickname()).isEqualTo("Amex");
        assertThat(response.type()).isEqualTo(PaymentMethodType.CREDIT_CARD);
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void getPaymentMethodsReturnsOnlyRequestedUserInNewestFirstOrder() {
        paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest("Amex", PaymentMethodType.CREDIT_CARD)
        );
        PaymentMethodResponse chase = paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest("Chase", PaymentMethodType.CREDIT_CARD)
        );
        User otherUser = userRepository.save(new User("User Two", "user2@example.com", "password-hash"));
        CurrentUserContext.set(otherUser);
        paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest("Cash", PaymentMethodType.CASH)
        );
        CurrentUserContext.set(currentUser);

        List<PaymentMethodResponse> responses = paymentMethodService.getPaymentMethods();

        assertThat(responses).extracting(PaymentMethodResponse::id).containsExactly(chase.id(), responses.get(1).id());
        assertThat(responses).extracting(PaymentMethodResponse::nickname).containsExactly("Chase", "Amex");
    }

    @Test
    void updatePaymentMethodChangesNicknameAndType() {
        PaymentMethodResponse created = paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest("Apple", PaymentMethodType.DIGITAL_WALLET)
        );

        PaymentMethodResponse updated = paymentMethodService.updatePaymentMethod(
                created.id(),
                new UpdatePaymentMethodRequest("Apple Pay", PaymentMethodType.DIGITAL_WALLET)
        );

        assertThat(updated.nickname()).isEqualTo("Apple Pay");
        assertThat(updated.type()).isEqualTo(PaymentMethodType.DIGITAL_WALLET);
    }

    @Test
    void deletePaymentMethodRemovesItForUser() {
        PaymentMethodResponse created = paymentMethodService.createPaymentMethod(
                new CreatePaymentMethodRequest("Cash", PaymentMethodType.CASH)
        );

        paymentMethodService.deletePaymentMethod(created.id());

        assertThatThrownBy(() -> paymentMethodService.getPaymentMethod(created.id()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment method not found with id " + created.id());
    }
}
