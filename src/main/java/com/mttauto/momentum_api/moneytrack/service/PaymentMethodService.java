package com.mttauto.momentum_api.moneytrack.service;

import com.mttauto.momentum_api.auth.CurrentUserContext;
import com.mttauto.momentum_api.moneytrack.dto.CreatePaymentMethodRequest;
import com.mttauto.momentum_api.moneytrack.dto.PaymentMethodResponse;
import com.mttauto.momentum_api.moneytrack.dto.UpdatePaymentMethodRequest;
import com.mttauto.momentum_api.moneytrack.entity.Expense;
import com.mttauto.momentum_api.moneytrack.entity.PaymentMethod;
import com.mttauto.momentum_api.exception.ResourceNotFoundException;
import com.mttauto.momentum_api.moneytrack.repository.ExpenseRepository;
import com.mttauto.momentum_api.moneytrack.repository.PaymentMethodRepository;
import com.mttauto.momentum_api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public PaymentMethodResponse createPaymentMethod(CreatePaymentMethodRequest request) {
        User user = CurrentUserContext.get();
        PaymentMethod paymentMethod = new PaymentMethod(
                user,
                request.nickname().trim(),
                request.type()
        );

        return toResponse(paymentMethodRepository.save(paymentMethod));
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getPaymentMethods() {
        return paymentMethodRepository.findByUser_IdOrderByCreatedAtDesc(CurrentUserContext.get().getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentMethodResponse getPaymentMethod(Long id) {
        return toResponse(findPaymentMethod(id, CurrentUserContext.get().getId()));
    }

    @Transactional
    public PaymentMethodResponse updatePaymentMethod(Long id, UpdatePaymentMethodRequest request) {
        PaymentMethod paymentMethod = findPaymentMethod(id, CurrentUserContext.get().getId());
        paymentMethod.update(request.nickname().trim(), request.type());

        return toResponse(paymentMethod);
    }

    @Transactional
    public void deletePaymentMethod(Long id) {
        PaymentMethod paymentMethod = findPaymentMethod(id, CurrentUserContext.get().getId());
        expenseRepository.findByPaymentMethod(paymentMethod).forEach(Expense::clearPaymentMethod);
        paymentMethodRepository.delete(paymentMethod);
    }

    PaymentMethod findPaymentMethod(Long id, Long userId) {
        return paymentMethodRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with id " + id));
    }

    PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(
                paymentMethod.getId(),
                paymentMethod.getUserId(),
                paymentMethod.getNickname(),
                paymentMethod.getType(),
                paymentMethod.getCreatedAt(),
                paymentMethod.getUpdatedAt()
        );
    }
}
