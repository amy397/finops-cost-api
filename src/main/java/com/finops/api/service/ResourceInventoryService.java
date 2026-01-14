package com.finops.api.service;

import com.finops.api.aws.*;
import com.finops.api.dto.resource.AwsResourceDto;
import com.finops.api.dto.resource.ResourceSummaryDto;
import com.finops.api.entity.AwsResource;
import com.finops.api.repository.AwsResourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ResourceInventoryService {

    private final AwsResourceRepository resourceRepository;
    private final Ec2ClientService ec2ClientService;
    private final RdsClientService rdsClientService;
    private final S3ClientService s3ClientService;
    private final LambdaClientService lambdaClientService;
    private final MockResourceService mockResourceService;

    @Autowired
    public ResourceInventoryService(
            AwsResourceRepository resourceRepository,
            @Autowired(required = false) Ec2ClientService ec2ClientService,
            @Autowired(required = false) RdsClientService rdsClientService,
            @Autowired(required = false) S3ClientService s3ClientService,
            @Autowired(required = false) LambdaClientService lambdaClientService,
            @Autowired(required = false) MockResourceService mockResourceService
    ) {
        this.resourceRepository = resourceRepository;
        this.ec2ClientService = ec2ClientService;
        this.rdsClientService = rdsClientService;
        this.s3ClientService = s3ClientService;
        this.lambdaClientService = lambdaClientService;
        this.mockResourceService = mockResourceService;
    }

    public List<AwsResourceDto> getAllResources() {
        log.debug("전체 리소스 조회");
        return resourceRepository.findAll().stream()
                .map(AwsResourceDto::from)
                .toList();
    }

    public Page<AwsResourceDto> getResourcesPaginated(Pageable pageable) {
        return resourceRepository.findAll(pageable)
                .map(AwsResourceDto::from);
    }

    public List<AwsResourceDto> getResourcesByType(String resourceType) {
        log.debug("리소스 타입별 조회: {}", resourceType);
        return resourceRepository.findByResourceType(resourceType.toUpperCase()).stream()
                .map(AwsResourceDto::from)
                .toList();
    }

    public Page<AwsResourceDto> getResourcesByTypePaginated(String resourceType, Pageable pageable) {
        return resourceRepository.findByResourceType(resourceType.toUpperCase(), pageable)
                .map(AwsResourceDto::from);
    }

    public AwsResourceDto getResourceById(String resourceId) {
        log.debug("리소스 조회: {}", resourceId);
        return resourceRepository.findByResourceId(resourceId)
                .map(AwsResourceDto::from)
                .orElse(null);
    }

    public List<AwsResourceDto> getIdleResources() {
        log.debug("유휴 리소스 조회");
        return resourceRepository.findIdleResources().stream()
                .map(AwsResourceDto::from)
                .toList();
    }

    public ResourceSummaryDto getResourceSummary() {
        log.debug("리소스 요약 조회");

        long total = resourceRepository.count();

        Map<String, Long> byType = resourceRepository.countByResourceType().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        Map<String, Long> byRegion = resourceRepository.countByRegion().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        long idle = resourceRepository.findIdleResources().size();

        return ResourceSummaryDto.of(total, byType, byRegion, idle);
    }

    @Transactional
    public int syncResources() {
        log.info("리소스 동기화 시작");
        List<AwsResource> resources;

        if (mockResourceService != null) {
            log.info("Mock 리소스 사용 (로컬 환경)");
            resources = mockResourceService.getAllMockResources();
        } else {
            log.info("AWS 리소스 동기화 (프로덕션 환경)");
            resources = new java.util.ArrayList<>();

            if (ec2ClientService != null) {
                resources.addAll(ec2ClientService.describeInstances());
            }
            if (rdsClientService != null) {
                resources.addAll(rdsClientService.describeDbInstances());
            }
            if (s3ClientService != null) {
                resources.addAll(s3ClientService.listBuckets());
            }
            if (lambdaClientService != null) {
                resources.addAll(lambdaClientService.listFunctions());
            }
        }

        int savedCount = 0;
        for (AwsResource resource : resources) {
            saveOrUpdateResource(resource);
            savedCount++;
        }

        log.info("리소스 동기화 완료: {} 개", savedCount);
        return savedCount;
    }

    @Transactional
    public int syncEc2Resources() {
        log.info("EC2 리소스 동기화");
        List<AwsResource> resources;

        if (mockResourceService != null) {
            resources = mockResourceService.getMockEc2Instances();
        } else if (ec2ClientService != null) {
            resources = ec2ClientService.describeInstances();
        } else {
            return 0;
        }

        int count = 0;
        for (AwsResource resource : resources) {
            saveOrUpdateResource(resource);
            count++;
        }
        return count;
    }

    @Transactional
    public int syncRdsResources() {
        log.info("RDS 리소스 동기화");
        List<AwsResource> resources;

        if (mockResourceService != null) {
            resources = mockResourceService.getMockRdsInstances();
        } else if (rdsClientService != null) {
            resources = rdsClientService.describeDbInstances();
        } else {
            return 0;
        }

        int count = 0;
        for (AwsResource resource : resources) {
            saveOrUpdateResource(resource);
            count++;
        }
        return count;
    }

    @Transactional
    public int syncS3Resources() {
        log.info("S3 리소스 동기화");
        List<AwsResource> resources;

        if (mockResourceService != null) {
            resources = mockResourceService.getMockS3Buckets();
        } else if (s3ClientService != null) {
            resources = s3ClientService.listBuckets();
        } else {
            return 0;
        }

        int count = 0;
        for (AwsResource resource : resources) {
            saveOrUpdateResource(resource);
            count++;
        }
        return count;
    }

    @Transactional
    public int syncLambdaResources() {
        log.info("Lambda 리소스 동기화");
        List<AwsResource> resources;

        if (mockResourceService != null) {
            resources = mockResourceService.getMockLambdaFunctions();
        } else if (lambdaClientService != null) {
            resources = lambdaClientService.listFunctions();
        } else {
            return 0;
        }

        int count = 0;
        for (AwsResource resource : resources) {
            saveOrUpdateResource(resource);
            count++;
        }
        return count;
    }

    private void saveOrUpdateResource(AwsResource resource) {
        resourceRepository.findByResourceId(resource.getResourceId())
                .ifPresentOrElse(
                        existing -> {
                            existing.setResourceName(resource.getResourceName());
                            existing.setRegion(resource.getRegion());
                            existing.setAvailabilityZone(resource.getAvailabilityZone());
                            existing.setState(resource.getState());
                            existing.setInstanceType(resource.getInstanceType());
                            existing.setTags(resource.getTags());
                            existing.setMetadata(resource.getMetadata());
                            existing.setLastSyncedAt(LocalDateTime.now());
                            resourceRepository.save(existing);
                        },
                        () -> resourceRepository.save(resource)
                );
    }
}
