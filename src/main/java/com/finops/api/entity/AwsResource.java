package com.finops.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "aws_resources", indexes = {
        @Index(name = "idx_aws_resources_type", columnList = "resource_type"),
        @Index(name = "idx_aws_resources_region", columnList = "region"),
        @Index(name = "idx_aws_resources_state", columnList = "state")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AwsResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_id", nullable = false, unique = true, length = 256)
    private String resourceId;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_name", length = 256)
    private String resourceName;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "availability_zone", length = 50)
    private String availabilityZone;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "instance_type", length = 50)
    private String instanceType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "text")
    private Map<String, String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "text")
    private Map<String, Object> metadata;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

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
