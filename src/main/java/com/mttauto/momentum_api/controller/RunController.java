package com.mttauto.momentum_api.controller;

import com.mttauto.momentum_api.dto.CreateRunRequest;
import com.mttauto.momentum_api.dto.RunResponse;
import com.mttauto.momentum_api.service.RunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class RunController {

    private final RunService runService;

    @PostMapping
    public ResponseEntity<RunResponse> createRun(@Valid @RequestBody CreateRunRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(runService.createRun(request));
    }

    @GetMapping
    public ResponseEntity<List<RunResponse>> getAllRuns() {
        return ResponseEntity.ok(runService.getAllRuns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RunResponse> getRunById(@PathVariable Long id) {
        return ResponseEntity.ok(runService.getRunById(id));
    }
}
