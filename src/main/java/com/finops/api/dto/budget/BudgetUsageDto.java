package com.finops.api.dto.budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BudgetUsageDto(
        Long budgetId,
        String budgetName,
        BigDecimal budgetAmount,
        BigDecimal actualAmount,
        BigDecimal remainingAmount,
        BigDecimal usagePercent,
        String status,  // ON_TRACK, WARNING, EXCEEDED
        LocalDate periodStart,
        LocalDate periodEnd,
        List<ThresholdStatus> thresholdStatuses
) {
    public static BudgetUsageDto of(
            Long budgetId,
            String budgetName,
            BigDecimal budgetAmount,
            BigDecimal actualAmount,
            LocalDate periodStart,
            LocalDate periodEnd,
            List<ThresholdStatus> thresholdStatuses
    ) {
        BigDecimal remaining = budgetAmount.subtract(actualAmount);
        BigDecimal usagePercent = budgetAmount.compareTo(BigDecimal.ZERO) > 0
                ? actualAmount.multiply(BigDecimal.valueOf(100)).divide(budgetAmount, 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String status;
        if (usagePercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            status = "EXCEEDED";
        } else if (usagePercent.compareTo(BigDecimal.valueOf(80)) > 0) {
            status = "WARNING";
        } else {
            status = "ON_TRACK";
        }

        return new BudgetUsageDto(
                budgetId,
                budgetName,
                budgetAmount,
                actualAmount,
                remaining,
                usagePercent,
                status,
                periodStart,
                periodEnd,
                thresholdStatuses
        );
    }

    public record ThresholdStatus(
            Integer thresholdPercent,
            boolean triggered,
            BigDecimal triggerAmount
    ) {}
}
