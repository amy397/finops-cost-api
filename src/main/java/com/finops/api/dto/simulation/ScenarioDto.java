package com.finops.api.dto.simulation;

import com.finops.api.entity.ScenarioItem;
import com.finops.api.entity.SimulationScenario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ScenarioDto(
        Long id,
        String name,
        String description,
        BigDecimal baseMonthCost,
        BigDecimal projectedMonthlyCost,
        BigDecimal costDifference,
        BigDecimal differencePercent,
        List<ItemDto> items,
        String createdBy,
        LocalDateTime createdAt
) {
    public static ScenarioDto from(SimulationScenario entity) {
        List<ItemDto> itemDtos = entity.getItems() != null
                ? entity.getItems().stream().map(ItemDto::from).toList()
                : List.of();

        return new ScenarioDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getBaseMonthCost(),
                entity.getProjectedMonthlyCost(),
                entity.getCostDifference(),
                entity.getDifferencePercent(),
                itemDtos,
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

    public record ItemDto(
            Long id,
            String actionType,
            String serviceCode,
            String resourceType,
            String instanceType,
            String region,
            Integer quantity,
            Integer usageHoursPerMonth,
            Integer storageGb,
            BigDecimal monthlyCost,
            String notes
    ) {
        public static ItemDto from(ScenarioItem entity) {
            return new ItemDto(
                    entity.getId(),
                    entity.getActionType(),
                    entity.getServiceCode(),
                    entity.getResourceType(),
                    entity.getInstanceType(),
                    entity.getRegion(),
                    entity.getQuantity(),
                    entity.getUsageHoursPerMonth(),
                    entity.getStorageGb(),
                    entity.getMonthlyCost(),
                    entity.getNotes()
            );
        }
    }
}
