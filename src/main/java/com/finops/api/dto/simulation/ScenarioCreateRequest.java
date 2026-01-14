package com.finops.api.dto.simulation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ScenarioCreateRequest(
        @NotBlank(message = "시나리오 이름은 필수입니다")
        @Size(max = 100)
        String name,

        String description,

        @NotEmpty(message = "최소 하나의 리소스 항목이 필요합니다")
        List<ItemRequest> items
) {
    public record ItemRequest(
            String actionType,  // ADD, REMOVE, MODIFY
            String serviceCode,
            String resourceType,
            String instanceType,
            String region,
            Integer quantity,
            Integer usageHoursPerMonth,
            Integer storageGb,
            String notes
    ) {}
}
