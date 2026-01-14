package com.finops.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_alerts", indexes = {
        @Index(name = "idx_budget_alerts_budget", columnList = "budget_id"),
        @Index(name = "idx_budget_alerts_sent", columnList = "sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @Column(name = "threshold_percent", nullable = false)
    private Integer thresholdPercent;

    @Column(name = "actual_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal actualAmount;

    @Column(name = "budget_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal budgetAmount;

    @Column(name = "usage_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal usagePercent;

    @Column(name = "alert_message")
    private String alertMessage;

    @Column(name = "sent_at")
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "slack_sent")
    @Builder.Default
    private Boolean slackSent = false;

    @Column(name = "email_sent")
    @Builder.Default
    private Boolean emailSent = false;
}
