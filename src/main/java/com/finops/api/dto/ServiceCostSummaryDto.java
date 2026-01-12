package com.finops.api.dto;

import java.math.BigDecimal;

public record ServiceCostSummaryDto(
        String serviceName,
        BigDecimal totalCost,
        BigDecimal percentage
) {
    public static ServiceCostSummaryDto of(String serviceName, BigDecimal totalCost, BigDecimal grandTotal) {
        BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                ? totalCost.multiply(BigDecimal.valueOf(100)).divide(grandTotal, 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new ServiceCostSummaryDto(serviceName, totalCost, percentage);
    }
}
