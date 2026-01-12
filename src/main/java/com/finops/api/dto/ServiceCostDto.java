package com.finops.api.dto;

import com.finops.api.entity.ServiceCost;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ServiceCostDto(
        Long id,
        LocalDate costDate,
        String serviceName,
        BigDecimal cost,
        String currency
) {
    public static ServiceCostDto from(ServiceCost entity) {
        return new ServiceCostDto(
                entity.getId(),
                entity.getCostDate(),
                entity.getServiceName(),
                entity.getCost(),
                entity.getCurrency()
        );
    }
}
