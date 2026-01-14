package com.finops.api.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public record ReportGenerateRequest(
        @NotBlank(message = "리포트 이름은 필수입니다")
        String reportName,

        @NotBlank(message = "리포트 타입은 필수입니다")
        @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|CUSTOM", message = "리포트 타입은 DAILY, WEEKLY, MONTHLY, CUSTOM 중 하나여야 합니다")
        String reportType,

        @NotBlank(message = "포맷은 필수입니다")
        @Pattern(regexp = "PDF|EXCEL", message = "포맷은 PDF 또는 EXCEL이어야 합니다")
        String format,

        @NotNull(message = "시작일은 필수입니다")
        LocalDate startDate,

        @NotNull(message = "종료일은 필수입니다")
        LocalDate endDate,

        List<String> recipients,  // Email addresses for sending

        Boolean sendEmail
) {}
