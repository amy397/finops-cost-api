package com.finops.api.dto.simulation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CalculationRequest(
        @NotEmpty(message = "최소 하나의 리소스 항목이 필요합니다")
        List<ResourceItem> items
) {
    public record ResourceItem(
            @NotNull(message = "서비스 코드는 필수입니다")
            String serviceCode,  // EC2, RDS, S3, LAMBDA

            String resourceType,

            String instanceType,

            String region,

            Integer quantity,

            Integer usageHoursPerMonth,

            Integer storageGb
    ) {}
}
