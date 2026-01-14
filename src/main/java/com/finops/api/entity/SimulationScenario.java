package com.finops.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulation_scenarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "base_monthly_cost", precision = 14, scale = 2)
    private BigDecimal baseMonthCost;

    @Column(name = "projected_monthly_cost", precision = 14, scale = 2)
    private BigDecimal projectedMonthlyCost;

    @Column(name = "cost_difference", precision = 14, scale = 2)
    private BigDecimal costDifference;

    @Column(name = "difference_percent", precision = 5, scale = 2)
    private BigDecimal differencePercent;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScenarioItem> items = new ArrayList<>();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(ScenarioItem item) {
        items.add(item);
        item.setScenario(this);
    }
}
