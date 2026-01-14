package com.finops.api.dto.resource;

import com.finops.api.entity.AwsResource;

import java.time.LocalDateTime;
import java.util.Map;

public record AwsResourceDto(
        Long id,
        String resourceId,
        String resourceType,
        String resourceName,
        String region,
        String availabilityZone,
        String state,
        String instanceType,
        Map<String, String> tags,
        Map<String, Object> metadata,
        LocalDateTime lastSyncedAt
) {
    public static AwsResourceDto from(AwsResource entity) {
        return new AwsResourceDto(
                entity.getId(),
                entity.getResourceId(),
                entity.getResourceType(),
                entity.getResourceName(),
                entity.getRegion(),
                entity.getAvailabilityZone(),
                entity.getState(),
                entity.getInstanceType(),
                entity.getTags(),
                entity.getMetadata(),
                entity.getLastSyncedAt()
        );
    }
}
