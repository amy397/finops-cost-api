package com.finops.api.dto;

import com.finops.api.entity.DailyCost;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyCostDto(
        Long id,
        LocalDate costDate,
        BigDecimal totalCost,
        String currency
) {
    public static DailyCostDto from(DailyCost entity) {
        return new DailyCostDto(
                entity.getId(),
                entity.getCostDate(),
                entity.getTotalCost(),
                entity.getCurrency()
        );
    }
}
