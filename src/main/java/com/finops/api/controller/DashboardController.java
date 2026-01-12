package com.finops.api.controller;

import com.finops.api.dto.CostSummaryDto;
import com.finops.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<CostSummaryDto> getCostSummary() {
        var summary = dashboardService.getCostSummary();
        return ResponseEntity.ok(summary);
    }
}
