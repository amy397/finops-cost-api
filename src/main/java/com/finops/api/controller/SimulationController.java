package com.finops.api.controller;

import com.finops.api.dto.simulation.*;
import com.finops.api.service.SimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @GetMapping("/scenarios")
    public ResponseEntity<List<ScenarioDto>> getAllScenarios() {
        return ResponseEntity.ok(simulationService.getAllScenarios());
    }

    @GetMapping("/scenarios/{id}")
    public ResponseEntity<ScenarioDto> getScenarioById(@PathVariable Long id) {
        return simulationService.getScenarioById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/calculate")
    public ResponseEntity<CostProjectionDto> calculateCost(
            @Valid @RequestBody CalculationRequest request
    ) {
        CostProjectionDto result = simulationService.calculateCost(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scenarios")
    public ResponseEntity<ScenarioDto> createScenario(
            @Valid @RequestBody ScenarioCreateRequest request
    ) {
        ScenarioDto created = simulationService.createScenario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/scenarios/{id}")
    public ResponseEntity<Void> deleteScenario(@PathVariable Long id) {
        simulationService.deleteScenario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pricing")
    public ResponseEntity<Map<String, Object>> getPricingData() {
        return ResponseEntity.ok(simulationService.getPricingData());
    }
}
