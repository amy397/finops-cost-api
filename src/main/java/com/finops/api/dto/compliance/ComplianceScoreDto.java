package com.finops.api.dto.compliance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record ComplianceScoreDto(
        BigDecimal overallScore,
        Integer totalResources,
        Integer compliantResources,
        Integer nonCompliantResources,
        Map<String, ResourceTypeCompliance> byResourceType,
        LocalDateTime lastScanDate
) {
    public static ComplianceScoreDto of(
            BigDecimal score,
            int total,
            int compliant,
            int nonCompliant,
            Map<String, ResourceTypeCompliance> byType,
            LocalDateTime lastScan
    ) {
        return new ComplianceScoreDto(score, total, compliant, nonCompliant, byType, lastScan);
    }

    public record ResourceTypeCompliance(
            String resourceType,
            int total,
            int compliant,
            int nonCompliant,
            BigDecimal score
    ) {}
}
