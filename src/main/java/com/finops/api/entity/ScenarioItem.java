package com.finops.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scenario_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private SimulationScenario scenario;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;  // ADD, REMOVE, MODIFY

    @Column(name = "service_code", nullable = false, length = 100)
    private String serviceCode;  // AmazonEC2, AmazonRDS, etc.

    @Column(name = "resource_type", length = 100)
    private String resourceType;  // Instance, Volume, etc.

    @Column(name = "instance_type", length = 50)
    private String instanceType;  // t3.micro, r5.large, etc.

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "usage_hours_per_month")
    @Builder.Default
    private Integer usageHoursPerMonth = 730;  // ~24 * 30.5

    @Column(name = "storage_gb")
    private Integer storageGb;

    @Column(name = "monthly_cost", precision = 14, scale = 2)
    private BigDecimal monthlyCost;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
