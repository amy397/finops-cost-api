package com.finops.api.dto.report;

import com.finops.api.entity.GeneratedReport;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GeneratedReportDto(
        Long id,
        String reportName,
        String reportType,
        String format,
        Long fileSizeBytes,
        LocalDate dateRangeStart,
        LocalDate dateRangeEnd,
        String generationStatus,
        String emailStatus,
        String errorMessage,
        LocalDateTime generatedAt,
        String downloadUrl
) {
    public static GeneratedReportDto from(GeneratedReport entity) {
        String downloadUrl = entity.getGenerationStatus().equals("COMPLETED")
                ? "/api/reports/download/" + entity.getId()
                : null;

        return new GeneratedReportDto(
                entity.getId(),
                entity.getReportName(),
                entity.getReportType(),
                entity.getFormat(),
                entity.getFileSizeBytes(),
                entity.getDateRangeStart(),
                entity.getDateRangeEnd(),
                entity.getGenerationStatus(),
                entity.getEmailStatus(),
                entity.getErrorMessage(),
                entity.getGeneratedAt(),
                downloadUrl
        );
    }
}
