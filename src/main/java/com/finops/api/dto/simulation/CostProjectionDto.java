package com.finops.api.dto.simulation;

import java.math.BigDecimal;
import java.util.List;

public record CostProjectionDto(
        BigDecimal currentMonthlyCost,
        BigDecimal projectedMonthlyCost,
        BigDecimal difference,
        BigDecimal differencePercent,
        List<ItemCost> itemCosts
) {
    public static CostProjectionDto of(
            BigDecimal current,
            BigDecimal projected,
            List<ItemCost> items
    ) {
        BigDecimal diff = projected.subtract(current);
        BigDecimal diffPercent = current.compareTo(BigDecimal.ZERO) > 0
                ? diff.multiply(BigDecimal.valueOf(100)).divide(current, 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new CostProjectionDto(current, projected, diff, diffPercent, items);
    }

    public record ItemCost(
            String serviceCode,
            String instanceType,
            String region,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal monthlyCost,
            String priceDescription
    ) {}
}
