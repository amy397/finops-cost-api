package com.finops.api.controller;

import com.finops.api.dto.resource.AwsResourceDto;
import com.finops.api.dto.resource.ResourceSummaryDto;
import com.finops.api.service.ResourceInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceInventoryService resourceInventoryService;

    @GetMapping
    public ResponseEntity<Page<AwsResourceDto>> getAllResources(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(resourceInventoryService.getResourcesPaginated(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<AwsResourceDto>> getAllResourcesList() {
        return ResponseEntity.ok(resourceInventoryService.getAllResources());
    }

    @GetMapping("/ec2")
    public ResponseEntity<Page<AwsResourceDto>> getEc2Instances(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(resourceInventoryService.getResourcesByTypePaginated("EC2", pageable));
    }

    @GetMapping("/rds")
    public ResponseEntity<Page<AwsResourceDto>> getRdsInstances(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(resourceInventoryService.getResourcesByTypePaginated("RDS", pageable));
    }

    @GetMapping("/s3")
    public ResponseEntity<Page<AwsResourceDto>> getS3Buckets(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(resourceInventoryService.getResourcesByTypePaginated("S3", pageable));
    }

    @GetMapping("/lambda")
    public ResponseEntity<Page<AwsResourceDto>> getLambdaFunctions(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(resourceInventoryService.getResourcesByTypePaginated("LAMBDA", pageable));
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<AwsResourceDto> getResourceById(@PathVariable String resourceId) {
        var resource = resourceInventoryService.getResourceById(resourceId);
        return resource != null
                ? ResponseEntity.ok(resource)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/idle")
    public ResponseEntity<List<AwsResourceDto>> getIdleResources() {
        return ResponseEntity.ok(resourceInventoryService.getIdleResources());
    }

    @GetMapping("/summary")
    public ResponseEntity<ResourceSummaryDto> getResourceSummary() {
        return ResponseEntity.ok(resourceInventoryService.getResourceSummary());
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncResources() {
        int count = resourceInventoryService.syncResources();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "리소스 동기화 완료",
                "syncedCount", count
        ));
    }

    @PostMapping("/sync/ec2")
    public ResponseEntity<Map<String, Object>> syncEc2Resources() {
        int count = resourceInventoryService.syncEc2Resources();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "EC2 리소스 동기화 완료",
                "syncedCount", count
        ));
    }

    @PostMapping("/sync/rds")
    public ResponseEntity<Map<String, Object>> syncRdsResources() {
        int count = resourceInventoryService.syncRdsResources();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "RDS 리소스 동기화 완료",
                "syncedCount", count
        ));
    }

    @PostMapping("/sync/s3")
    public ResponseEntity<Map<String, Object>> syncS3Resources() {
        int count = resourceInventoryService.syncS3Resources();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "S3 리소스 동기화 완료",
                "syncedCount", count
        ));
    }

    @PostMapping("/sync/lambda")
    public ResponseEntity<Map<String, Object>> syncLambdaResources() {
        int count = resourceInventoryService.syncLambdaResources();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lambda 리소스 동기화 완료",
                "syncedCount", count
        ));
    }
}
