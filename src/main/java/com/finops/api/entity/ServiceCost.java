package com.finops.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_costs", indexes = {
    @Index(name = "idx_service_costs_date", columnList = "cost_date"),
    @Index(name = "idx_service_costs_service", columnList = "service_name")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cost_date", nullable = false)
    private LocalDate costDate;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "cost", nullable = false, precision = 12, scale = 4)
    private BigDecimal cost;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
