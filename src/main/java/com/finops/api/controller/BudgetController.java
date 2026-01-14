package com.finops.api.controller;

import com.finops.api.dto.budget.*;
import com.finops.api.entity.BudgetAlert;
import com.finops.api.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetDto>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetDto> getBudgetById(@PathVariable Long id) {
        return budgetService.getBudgetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BudgetDto> createBudget(@Valid @RequestBody BudgetCreateRequest request) {
        BudgetDto created = budgetService.createBudget(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetCreateRequest request
    ) {
        BudgetDto updated = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/usage")
    public ResponseEntity<BudgetUsageDto> getBudgetUsage(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetUsage(id));
    }

    @GetMapping("/{id}/alerts")
    public ResponseEntity<List<BudgetAlert>> getBudgetAlerts(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetAlerts(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<BudgetDashboardDto> getBudgetDashboard() {
        return ResponseEntity.ok(budgetService.getBudgetDashboard());
    }

    @PostMapping("/check-thresholds")
    public ResponseEntity<Map<String, String>> checkThresholds() {
        budgetService.checkBudgetThresholds();
        return ResponseEntity.ok(Map.of("message", "임계값 체크 완료"));
    }
}
