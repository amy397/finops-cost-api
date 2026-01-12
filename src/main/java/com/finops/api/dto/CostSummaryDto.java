package com.finops.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CostSummaryDto(
        BigDecimal currentMonthCost,
        BigDecimal previousMonthCost,
        BigDecimal changePercent,
        BigDecimal todayCost,
        BigDecimal yesterdayCost,
        List<ServiceCostSummaryDto> topServices
) {
    public static CostSummaryDto of(
            BigDecimal currentMonthCost,
            BigDecimal previousMonthCost,
            BigDecimal todayCost,
            BigDecimal yesterdayCost,
            List<ServiceCostSummaryDto> topServices
    ) {
        BigDecimal changePercent = calculateChangePercent(currentMonthCost, previousMonthCost);
        return new CostSummaryDto(
                currentMonthCost,
                previousMonthCost,
                changePercent,
                todayCost,
                yesterdayCost,
                topServices
        );
    }

    private static BigDecimal calculateChangePercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, java.math.RoundingMode.HALF_UP);
    }
}
