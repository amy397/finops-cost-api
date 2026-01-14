package com.finops.api.dto.budget;

import java.math.BigDecimal;
import java.util.List;

public record BudgetDashboardDto(
        int totalBudgets,
        int activeBudgets,
        BigDecimal totalBudgetAmount,
        BigDecimal totalActualAmount,
        BigDecimal overallUsagePercent,
        int exceededCount,
        int warningCount,
        int onTrackCount,
        List<BudgetUsageDto> budgetUsages
) {
    public static BudgetDashboardDto of(List<BudgetUsageDto> usages) {
        int total = usages.size();
        int active = total;

        BigDecimal totalBudget = usages.stream()
                .map(BudgetUsageDto::budgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActual = usages.stream()
                .map(BudgetUsageDto::actualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overallPercent = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalActual.multiply(BigDecimal.valueOf(100)).divide(totalBudget, 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int exceeded = (int) usages.stream().filter(u -> "EXCEEDED".equals(u.status())).count();
        int warning = (int) usages.stream().filter(u -> "WARNING".equals(u.status())).count();
        int onTrack = (int) usages.stream().filter(u -> "ON_TRACK".equals(u.status())).count();

        return new BudgetDashboardDto(
                total,
                active,
                totalBudget,
                totalActual,
                overallPercent,
                exceeded,
                warning,
                onTrack,
                usages
        );
    }
}
