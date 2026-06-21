package com.mttauto.momentum_api.moneytrack.dto;

import com.mttauto.momentum_api.moneytrack.entity.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePaymentMethodRequest(
        @NotBlank(message = "nickname is required")
        @Size(max = 80, message = "nickname must be 80 characters or fewer")
        String nickname,

        @NotNull(message = "type is required")
        PaymentMethodType type
) {
}
