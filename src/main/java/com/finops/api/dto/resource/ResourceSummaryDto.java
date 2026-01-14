package com.finops.api.dto.resource;

import java.util.Map;

public record ResourceSummaryDto(
        long totalResources,
        Map<String, Long> byType,
        Map<String, Long> byRegion,
        long idleResources
) {
    public static ResourceSummaryDto of(
            long total,
            Map<String, Long> byType,
            Map<String, Long> byRegion,
            long idle
    ) {
        return new ResourceSummaryDto(total, byType, byRegion, idle);
    }
}
