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
@Table(name = "daily_costs", indexes = {
    @Index(name = "idx_daily_costs_date", columnList = "cost_date")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cost_date", nullable = false)
    private LocalDate costDate;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 4)
    private BigDecimal totalCost;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
