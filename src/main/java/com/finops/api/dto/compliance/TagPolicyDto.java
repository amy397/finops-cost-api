package com.finops.api.dto.compliance;

import com.finops.api.entity.RequiredTag;
import com.finops.api.entity.TaggingPolicy;

import java.time.LocalDateTime;
import java.util.List;

public record TagPolicyDto(
        Long id,
        String name,
        String description,
        Boolean isActive,
        List<RequiredTagDto> requiredTags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TagPolicyDto from(TaggingPolicy entity) {
        List<RequiredTagDto> tags = entity.getRequiredTags() != null
                ? entity.getRequiredTags().stream().map(RequiredTagDto::from).toList()
                : List.of();

        return new TagPolicyDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getIsActive(),
                tags,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public record RequiredTagDto(
            Long id,
            String tagKey,
            String allowedValues,
            Boolean isMandatory
    ) {
        public static RequiredTagDto from(RequiredTag entity) {
            return new RequiredTagDto(
                    entity.getId(),
                    entity.getTagKey(),
                    entity.getAllowedValues(),
                    entity.getIsMandatory()
            );
        }
    }
}
