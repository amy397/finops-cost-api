package com.finops.api.dto.compliance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TagPolicyCreateRequest(
        @NotBlank(message = "정책 이름은 필수입니다")
        @Size(max = 100)
        String name,

        String description,

        @NotEmpty(message = "최소 하나의 필수 태그가 필요합니다")
        List<RequiredTagRequest> requiredTags
) {
    public record RequiredTagRequest(
            @NotBlank(message = "태그 키는 필수입니다")
            @Size(max = 128)
            String tagKey,

            String allowedValues,  // JSON array or null

            Boolean isMandatory
    ) {}
}
