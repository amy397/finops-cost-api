package com.finops.api.controller;

import com.finops.api.dto.DailyCostDto;
import com.finops.api.service.DailyCostService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/costs/daily")
@RequiredArgsConstructor
public class DailyCostController {

    private final DailyCostService dailyCostService;

    @GetMapping
    public ResponseEntity<List<DailyCostDto>> getDailyCosts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        var costs = dailyCostService.getDailyCosts(startDate, endDate);
        return ResponseEntity.ok(costs);
    }

    @GetMapping("/today")
    public ResponseEntity<DailyCostDto> getTodayCost() {
        var cost = dailyCostService.getTodayCost();
        return cost != null
                ? ResponseEntity.ok(cost)
                : ResponseEntity.noContent().build();
    }

    @GetMapping("/last-week")
    public ResponseEntity<List<DailyCostDto>> getLastWeekCosts() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        var costs = dailyCostService.getDailyCosts(startDate, endDate);
        return ResponseEntity.ok(costs);
    }

    @GetMapping("/last-month")
    public ResponseEntity<List<DailyCostDto>> getLastMonthCosts() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        var costs = dailyCostService.getDailyCosts(startDate, endDate);
        return ResponseEntity.ok(costs);
    }
}
