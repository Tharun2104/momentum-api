package com.mttauto.momentum_api.moneytrack.controller;

import com.mttauto.momentum_api.moneytrack.dto.SharedExpenseResponse;
import com.mttauto.momentum_api.moneytrack.service.SharedExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shared-expenses")
@RequiredArgsConstructor
public class SharedExpenseController {

    private final SharedExpenseService sharedExpenseService;

    @GetMapping
    public ResponseEntity<List<SharedExpenseResponse>> getSharedExpenses() {
        return ResponseEntity.ok(sharedExpenseService.getSharedExpenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SharedExpenseResponse> getSharedExpense(@PathVariable Long id) {
        return ResponseEntity.ok(sharedExpenseService.getSharedExpense(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSharedExpense(@PathVariable Long id) {
        sharedExpenseService.deleteSharedExpense(id);
        return ResponseEntity.noContent().build();
    }
}
