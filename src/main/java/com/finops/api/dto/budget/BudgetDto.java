package com.finops.api.dto.budget;

import com.finops.api.entity.Budget;
import com.finops.api.entity.BudgetThreshold;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BudgetDto(
        Long id,
        String name,
        String budgetType,
        String targetId,
        BigDecimal amount,
        String periodType,
        LocalDate startDate,
        LocalDate endDate,
        String currency,
        String description,
        Boolean isActive,
        List<ThresholdDto> thresholds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BudgetDto from(Budget entity) {
        List<ThresholdDto> thresholdDtos = entity.getThresholds() != null
                ? entity.getThresholds().stream().map(ThresholdDto::from).toList()
                : List.of();

        return new BudgetDto(
                entity.getId(),
                entity.getName(),
                entity.getBudgetType(),
                entity.getTargetId(),
                entity.getAmount(),
                entity.getPeriodType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCurrency(),
                entity.getDescription(),
                entity.getIsActive(),
                thresholdDtos,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public record ThresholdDto(
            Long id,
            Integer thresholdPercent,
            String notificationType,
            Boolean isActive,
            LocalDateTime lastTriggeredAt
    ) {
        public static ThresholdDto from(BudgetThreshold entity) {
            return new ThresholdDto(
                    entity.getId(),
                    entity.getThresholdPercent(),
                    entity.getNotificationType(),
                    entity.getIsActive(),
                    entity.getLastTriggeredAt()
            );
        }
    }
}
