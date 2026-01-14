package com.finops.api.dto.compliance;

import com.finops.api.entity.NonCompliantResource;
import com.finops.api.entity.TagComplianceScan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComplianceScanDto(
        Long id,
        LocalDateTime scanDate,
        Integer totalResources,
        Integer compliantResources,
        Integer nonCompliantResources,
        BigDecimal complianceScore,
        List<NonCompliantResourceDto> nonCompliantResourceList
) {
    public static ComplianceScanDto from(TagComplianceScan entity) {
        List<NonCompliantResourceDto> nonCompliant = entity.getNonCompliantResourceList() != null
                ? entity.getNonCompliantResourceList().stream().map(NonCompliantResourceDto::from).toList()
                : List.of();

        return new ComplianceScanDto(
                entity.getId(),
                entity.getScanDate(),
                entity.getTotalResources(),
                entity.getCompliantResources(),
                entity.getNonCompliantResources(),
                entity.getComplianceScore(),
                nonCompliant
        );
    }

    public static ComplianceScanDto fromWithoutDetails(TagComplianceScan entity) {
        return new ComplianceScanDto(
                entity.getId(),
                entity.getScanDate(),
                entity.getTotalResources(),
                entity.getCompliantResources(),
                entity.getNonCompliantResources(),
                entity.getComplianceScore(),
                List.of()
        );
    }

    public record NonCompliantResourceDto(
            Long id,
            String resourceId,
            String resourceType,
            String resourceName,
            String region,
            String missingTags,
            String invalidTags
    ) {
        public static NonCompliantResourceDto from(NonCompliantResource entity) {
            return new NonCompliantResourceDto(
                    entity.getId(),
                    entity.getResourceId(),
                    entity.getResourceType(),
                    entity.getResourceName(),
                    entity.getRegion(),
                    entity.getMissingTags(),
                    entity.getInvalidTags()
            );
        }
    }
}
