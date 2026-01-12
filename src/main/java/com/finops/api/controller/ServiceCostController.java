package com.finops.api.controller;

import com.finops.api.dto.ServiceCostDto;
import com.finops.api.dto.ServiceCostSummaryDto;
import com.finops.api.service.ServiceCostService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/costs/services")
@RequiredArgsConstructor
public class ServiceCostController {

    private final ServiceCostService serviceCostService;

    @GetMapping
    public ResponseEntity<List<ServiceCostDto>> getServiceCosts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        var costs = serviceCostService.getServiceCosts(startDate, endDate);
        return ResponseEntity.ok(costs);
    }

    @GetMapping("/summary")
    public ResponseEntity<List<ServiceCostSummaryDto>> getServiceCostSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        var summary = serviceCostService.getServiceCostSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/top")
    public ResponseEntity<List<ServiceCostSummaryDto>> getTopServices(
            @RequestParam(defaultValue = "5") int limit
    ) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.withDayOfMonth(1);
        var topServices = serviceCostService.getTopServices(startDate, endDate, limit);
        return ResponseEntity.ok(topServices);
    }
}
