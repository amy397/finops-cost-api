package com.finops.api.dto.budget;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BudgetCreateRequest(
        @NotBlank(message = "예산 이름은 필수입니다")
        @Size(max = 100)
        String name,

        @NotBlank(message = "예산 타입은 필수입니다")
        @Pattern(regexp = "TEAM|PROJECT|SERVICE", message = "예산 타입은 TEAM, PROJECT, SERVICE 중 하나여야 합니다")
        String budgetType,

        String targetId,

        @NotNull(message = "예산 금액은 필수입니다")
        @DecimalMin(value = "0.01", message = "예산 금액은 0보다 커야 합니다")
        BigDecimal amount,

        @NotBlank(message = "기간 타입은 필수입니다")
        @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY", message = "기간 타입은 MONTHLY, QUARTERLY, YEARLY 중 하나여야 합니다")
        String periodType,

        @NotNull(message = "시작일은 필수입니다")
        LocalDate startDate,

        LocalDate endDate,

        String currency,

        String description,

        List<ThresholdRequest> thresholds
) {
    public record ThresholdRequest(
            @NotNull(message = "임계값 퍼센트는 필수입니다")
            @Min(value = 1, message = "임계값은 1% 이상이어야 합니다")
            @Max(value = 200, message = "임계값은 200% 이하여야 합니다")
            Integer thresholdPercent,

            @Pattern(regexp = "EMAIL|SLACK|BOTH", message = "알림 타입은 EMAIL, SLACK, BOTH 중 하나여야 합니다")
            String notificationType
    ) {}
}
