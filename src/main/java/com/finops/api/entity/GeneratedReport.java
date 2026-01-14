package com.finops.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "generated_reports", indexes = {
        @Index(name = "idx_generated_reports_date", columnList = "generated_at"),
        @Index(name = "idx_generated_reports_status", columnList = "generation_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_name", nullable = false, length = 255)
    private String reportName;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;  // DAILY, WEEKLY, MONTHLY, CUSTOM

    @Column(name = "format", nullable = false, length = 20)
    private String format;  // PDF, EXCEL

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "date_range_start")
    private LocalDate dateRangeStart;

    @Column(name = "date_range_end")
    private LocalDate dateRangeEnd;

    @Column(name = "generation_status", length = 50)
    @Builder.Default
    private String generationStatus = "PENDING";  // PENDING, GENERATING, COMPLETED, FAILED

    @Column(name = "email_status", length = 50)
    @Builder.Default
    private String emailStatus = "NOT_SENT";  // NOT_SENT, SENDING, SENT, FAILED

    @Column(name = "recipients")
    private String recipients;  // JSON array of email addresses

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "generated_at")
    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
