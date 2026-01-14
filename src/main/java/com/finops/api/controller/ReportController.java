package com.finops.api.controller;

import com.finops.api.dto.report.GeneratedReportDto;
import com.finops.api.dto.report.ReportGenerateRequest;
import com.finops.api.service.ReportGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportGenerationService reportGenerationService;

    @GetMapping
    public ResponseEntity<List<GeneratedReportDto>> getRecentReports(
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(reportGenerationService.getRecentReports(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneratedReportDto> getReportById(@PathVariable Long id) {
        return reportGenerationService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/generate")
    public ResponseEntity<GeneratedReportDto> generateReport(
            @Valid @RequestBody ReportGenerateRequest request
    ) {
        GeneratedReportDto report = reportGenerationService.generateReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        try {
            byte[] fileContent = reportGenerationService.getReportFile(id);
            String contentType = reportGenerationService.getReportContentType(id);
            String filename = reportGenerationService.getReportFilename(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(fileContent.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
