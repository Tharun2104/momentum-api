package com.mttauto.momentum_api.moneytrack.controller;

import com.mttauto.momentum_api.moneytrack.dto.CreatePaymentMethodRequest;
import com.mttauto.momentum_api.moneytrack.dto.PaymentMethodResponse;
import com.mttauto.momentum_api.moneytrack.dto.UpdatePaymentMethodRequest;
import com.mttauto.momentum_api.moneytrack.service.PaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Payment Methods", description = "User-created payment labels for manual expense tracking.")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    @Operation(summary = "Create a payment method label")
    public ResponseEntity<PaymentMethodResponse> createPaymentMethod(
            @Valid @RequestBody CreatePaymentMethodRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMethodService.createPaymentMethod(request));
    }

    @GetMapping
    @Operation(summary = "List payment method labels for a user")
    public ResponseEntity<List<PaymentMethodResponse>> getPaymentMethods() {
        return ResponseEntity.ok(paymentMethodService.getPaymentMethods());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment method label")
    public ResponseEntity<PaymentMethodResponse> getPaymentMethod(@PathVariable Long id) {
        return ResponseEntity.ok(paymentMethodService.getPaymentMethod(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a payment method label")
    public ResponseEntity<PaymentMethodResponse> updatePaymentMethod(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentMethodRequest request
    ) {
        return ResponseEntity.ok(paymentMethodService.updatePaymentMethod(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a payment method label")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long id) {
        paymentMethodService.deletePaymentMethod(id);
        return ResponseEntity.noContent().build();
    }
}
