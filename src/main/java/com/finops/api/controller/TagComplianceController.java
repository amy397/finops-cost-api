package com.finops.api.controller;

import com.finops.api.dto.compliance.*;
import com.finops.api.service.TagComplianceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag-compliance")
@RequiredArgsConstructor
public class TagComplianceController {

    private final TagComplianceService tagComplianceService;

    // Policy endpoints
    @GetMapping("/policies")
    public ResponseEntity<List<TagPolicyDto>> getAllPolicies() {
        return ResponseEntity.ok(tagComplianceService.getAllPolicies());
    }

    @GetMapping("/policies/{id}")
    public ResponseEntity<TagPolicyDto> getPolicyById(@PathVariable Long id) {
        return tagComplianceService.getPolicyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/policies")
    public ResponseEntity<TagPolicyDto> createPolicy(@Valid @RequestBody TagPolicyCreateRequest request) {
        TagPolicyDto created = tagComplianceService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/policies/{id}")
    public ResponseEntity<TagPolicyDto> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody TagPolicyCreateRequest request
    ) {
        TagPolicyDto updated = tagComplianceService.updatePolicy(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/policies/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        tagComplianceService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }

    // Scan endpoints
    @PostMapping("/scan")
    public ResponseEntity<ComplianceScanDto> runComplianceScan() {
        ComplianceScanDto result = tagComplianceService.runComplianceScan();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/scans")
    public ResponseEntity<List<ComplianceScanDto>> getRecentScans(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(tagComplianceService.getRecentScans(limit));
    }

    @GetMapping("/scans/{id}")
    public ResponseEntity<ComplianceScanDto> getScanById(@PathVariable Long id) {
        return tagComplianceService.getScanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/score")
    public ResponseEntity<ComplianceScoreDto> getCurrentScore() {
        return ResponseEntity.ok(tagComplianceService.getCurrentScore());
    }
}
