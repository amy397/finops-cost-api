package com.finops.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "non_compliant_resources", indexes = {
        @Index(name = "idx_non_compliant_scan", columnList = "scan_id"),
        @Index(name = "idx_non_compliant_type", columnList = "resource_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonCompliantResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id", nullable = false)
    private TagComplianceScan scan;

    @Column(name = "resource_id", nullable = false, length = 256)
    private String resourceId;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_name", length = 256)
    private String resourceName;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "missing_tags")
    private String missingTags;  // JSON array

    @Column(name = "invalid_tags")
    private String invalidTags;  // JSON array

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private TaggingPolicy policy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
