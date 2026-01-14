package com.finops.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tag_compliance_scans", indexes = {
        @Index(name = "idx_tag_scans_date", columnList = "scan_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagComplianceScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scan_date", nullable = false)
    private LocalDateTime scanDate;

    @Column(name = "total_resources", nullable = false)
    private Integer totalResources;

    @Column(name = "compliant_resources", nullable = false)
    private Integer compliantResources;

    @Column(name = "non_compliant_resources", nullable = false)
    private Integer nonCompliantResources;

    @Column(name = "compliance_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal complianceScore;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NonCompliantResource> nonCompliantResourceList = new ArrayList<>();

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void addNonCompliantResource(NonCompliantResource resource) {
        nonCompliantResourceList.add(resource);
        resource.setScan(this);
    }
}
