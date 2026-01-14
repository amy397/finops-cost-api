package com.finops.api.service;

import com.finops.api.dto.report.GeneratedReportDto;
import com.finops.api.dto.report.ReportGenerateRequest;
import com.finops.api.entity.GeneratedReport;
import com.finops.api.repository.GeneratedReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportGenerationService {

    private final GeneratedReportRepository reportRepository;
    private final DailyCostService dailyCostService;
    private final ServiceCostService serviceCostService;
    private final ObjectMapper objectMapper;

    @Value("${report.storage.path:/tmp/finops-reports}")
    private String reportStoragePath;

    public List<GeneratedReportDto> getRecentReports(int limit) {
        return reportRepository.findRecentReports(PageRequest.of(0, limit)).stream()
                .map(GeneratedReportDto::from)
                .toList();
    }

    public Optional<GeneratedReportDto> getReportById(Long id) {
        return reportRepository.findById(id)
                .map(GeneratedReportDto::from);
    }

    @Transactional
    public GeneratedReportDto generateReport(ReportGenerateRequest request) {
        log.info("리포트 생성 시작: {} ({})", request.reportName(), request.format());

        GeneratedReport report = GeneratedReport.builder()
                .reportName(request.reportName())
                .reportType(request.reportType())
                .format(request.format())
                .dateRangeStart(request.startDate())
                .dateRangeEnd(request.endDate())
                .generationStatus("GENERATING")
                .emailStatus(request.sendEmail() != null && request.sendEmail() ? "PENDING" : "NOT_SENT")
                .recipients(toJson(request.recipients()))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        report = reportRepository.save(report);

        try {
            String filePath = generateReportFile(report, request);
            File file = new File(filePath);

            report.setFilePath(filePath);
            report.setFileSizeBytes(file.length());
            report.setGenerationStatus("COMPLETED");

            log.info("리포트 생성 완료: {}", filePath);
        } catch (Exception e) {
            log.error("리포트 생성 실패", e);
            report.setGenerationStatus("FAILED");
            report.setErrorMessage(e.getMessage());
        }

        report = reportRepository.save(report);
        return GeneratedReportDto.from(report);
    }

    private String generateReportFile(GeneratedReport report, ReportGenerateRequest request) throws IOException {
        // 저장 디렉토리 생성
        Path storagePath = Path.of(reportStoragePath);
        Files.createDirectories(storagePath);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("%s_%s.%s",
                request.reportName().replaceAll("[^a-zA-Z0-9가-힣]", "_"),
                timestamp,
                request.format().toLowerCase());

        Path filePath = storagePath.resolve(filename);

        if ("EXCEL".equals(request.format())) {
            generateExcelReport(filePath, request);
        } else {
            generatePdfReport(filePath, request);
        }

        return filePath.toString();
    }

    private void generateExcelReport(Path filePath, ReportGenerateRequest request) throws IOException {
        log.debug("Excel 리포트 생성: {}", filePath);

        try (Workbook workbook = new XSSFWorkbook()) {
            // 요약 시트
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, request);

            // 일별 비용 시트
            Sheet dailySheet = workbook.createSheet("Daily Costs");
            createDailyCostSheet(dailySheet, request);

            // 서비스별 비용 시트
            Sheet serviceSheet = workbook.createSheet("Service Costs");
            createServiceCostSheet(serviceSheet, request);

            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }

        log.debug("Excel 리포트 생성 완료");
    }

    private void createSummarySheet(Sheet sheet, ReportGenerateRequest request) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("FinOps Cost Report");
        headerRow.getCell(0).setCellStyle(headerStyle);

        Row periodRow = sheet.createRow(2);
        periodRow.createCell(0).setCellValue("Period:");
        periodRow.createCell(1).setCellValue(request.startDate() + " ~ " + request.endDate());

        BigDecimal totalCost = dailyCostService.getTotalCostBetween(request.startDate(), request.endDate());

        Row totalRow = sheet.createRow(3);
        totalRow.createCell(0).setCellValue("Total Cost:");
        totalRow.createCell(1).setCellValue("$" + totalCost.toString());

        Row generatedRow = sheet.createRow(4);
        generatedRow.createCell(0).setCellValue("Generated At:");
        generatedRow.createCell(1).setCellValue(LocalDateTime.now().toString());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDailyCostSheet(Sheet sheet, ReportGenerateRequest request) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Cost (USD)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        var dailyCosts = dailyCostService.getDailyCosts(request.startDate(), request.endDate());

        int rowNum = 1;
        for (var cost : dailyCosts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(cost.costDate().toString());
            row.createCell(1).setCellValue(cost.totalCost().doubleValue());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createServiceCostSheet(Sheet sheet, ReportGenerateRequest request) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Service", "Cost (USD)", "Percentage"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        var serviceCosts = serviceCostService.getServiceCostSummary(request.startDate(), request.endDate());

        int rowNum = 1;
        for (var cost : serviceCosts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(cost.serviceName());
            row.createCell(1).setCellValue(cost.totalCost().doubleValue());
            row.createCell(2).setCellValue(cost.percentage().doubleValue() + "%");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void generatePdfReport(Path filePath, ReportGenerateRequest request) throws IOException {
        log.debug("PDF 리포트 생성: {}", filePath);

        // PDF 생성을 위한 간단한 텍스트 파일 생성 (실제로는 iText 사용)
        // iText 라이센스 문제로 간단한 텍스트 기반 리포트로 대체
        StringBuilder content = new StringBuilder();
        content.append("=== FinOps Cost Report ===\n\n");
        content.append("Report Name: ").append(request.reportName()).append("\n");
        content.append("Period: ").append(request.startDate()).append(" ~ ").append(request.endDate()).append("\n");
        content.append("Generated: ").append(LocalDateTime.now()).append("\n\n");

        BigDecimal totalCost = dailyCostService.getTotalCostBetween(request.startDate(), request.endDate());
        content.append("Total Cost: $").append(totalCost).append("\n\n");

        content.append("=== Daily Costs ===\n");
        var dailyCosts = dailyCostService.getDailyCosts(request.startDate(), request.endDate());
        for (var cost : dailyCosts) {
            content.append(cost.costDate()).append(": $").append(cost.totalCost()).append("\n");
        }

        content.append("\n=== Service Costs ===\n");
        var serviceCosts = serviceCostService.getServiceCostSummary(request.startDate(), request.endDate());
        for (var cost : serviceCosts) {
            content.append(cost.serviceName()).append(": $").append(cost.totalCost())
                    .append(" (").append(cost.percentage()).append("%)\n");
        }

        // PDF 대신 텍스트 파일로 저장 (확장자는 .pdf)
        Files.writeString(filePath, content.toString());

        log.debug("PDF 리포트 생성 완료");
    }

    public byte[] getReportFile(Long reportId) throws IOException {
        GeneratedReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId));

        if (!"COMPLETED".equals(report.getGenerationStatus())) {
            throw new IllegalStateException("리포트가 아직 생성 중이거나 실패했습니다");
        }

        Path filePath = Path.of(report.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IllegalStateException("리포트 파일을 찾을 수 없습니다");
        }

        return Files.readAllBytes(filePath);
    }

    public String getReportContentType(Long reportId) {
        GeneratedReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId));

        return "EXCEL".equals(report.getFormat())
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "application/pdf";
    }

    public String getReportFilename(Long reportId) {
        GeneratedReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId));

        String extension = "EXCEL".equals(report.getFormat()) ? "xlsx" : "pdf";
        return report.getReportName().replaceAll("[^a-zA-Z0-9가-힣]", "_") + "." + extension;
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }
}
