package com.mttauto.momentum_api.moneytrack.controller;

import com.mttauto.momentum_api.moneytrack.dto.FriendBalanceResponse;
import com.mttauto.momentum_api.moneytrack.dto.SharedExpenseResponse;
import com.mttauto.momentum_api.moneytrack.dto.SplitsSummaryResponse;
import com.mttauto.momentum_api.moneytrack.service.SplitsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/splits")
@RequiredArgsConstructor
public class SplitsController {

    private final SplitsService splitsService;

    @GetMapping("/summary")
    public ResponseEntity<SplitsSummaryResponse> getSummary() {
        return ResponseEntity.ok(splitsService.getSummary());
    }

    @GetMapping("/friend-balances")
    public ResponseEntity<List<FriendBalanceResponse>> getFriendBalances() {
        return ResponseEntity.ok(splitsService.getFriendBalances());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SharedExpenseResponse>> getRecent() {
        return ResponseEntity.ok(splitsService.getRecent());
    }
}
