package com.mttauto.momentum_api.moneytrack.controller;

import com.mttauto.momentum_api.moneytrack.dto.CategorySummaryResponse;
import com.mttauto.momentum_api.moneytrack.dto.MonthlySummaryResponse;
import com.mttauto.momentum_api.moneytrack.dto.PaymentMethodSummaryResponse;
import com.mttauto.momentum_api.moneytrack.service.FinanceAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Finance Analytics", description = "Lightweight expense awareness summaries.")
public class FinanceAnalyticsController {

    private final FinanceAnalyticsService financeAnalyticsService;

    @GetMapping("/monthly-summary")
    @Operation(summary = "Get monthly spending summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(financeAnalyticsService.getMonthlySummary(month));
    }

    @GetMapping("/category-summary")
    @Operation(summary = "Get spending by category for a month")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(financeAnalyticsService.getCategorySummary(month));
    }

    @GetMapping("/payment-method-summary")
    @Operation(summary = "Get spending by payment method for a month")
    public ResponseEntity<List<PaymentMethodSummaryResponse>> getPaymentMethodSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(financeAnalyticsService.getPaymentMethodSummary(month));
    }
}
