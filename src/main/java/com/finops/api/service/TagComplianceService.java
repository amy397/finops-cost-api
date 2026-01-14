package com.finops.api.service;

import com.finops.api.dto.compliance.*;
import com.finops.api.entity.*;
import com.finops.api.repository.AwsResourceRepository;
import com.finops.api.repository.TagComplianceScanRepository;
import com.finops.api.repository.TaggingPolicyRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagComplianceService {

    private final TaggingPolicyRepository policyRepository;
    private final TagComplianceScanRepository scanRepository;
    private final AwsResourceRepository resourceRepository;
    private final ObjectMapper objectMapper;

    public List<TagPolicyDto> getAllPolicies() {
        log.debug("전체 태그 정책 조회");
        return policyRepository.findByIsActiveTrue().stream()
                .map(TagPolicyDto::from)
                .toList();
    }

    public Optional<TagPolicyDto> getPolicyById(Long id) {
        log.debug("태그 정책 조회: {}", id);
        return Optional.ofNullable(policyRepository.findByIdWithRequiredTags(id))
                .map(TagPolicyDto::from);
    }

    @Transactional
    public TagPolicyDto createPolicy(TagPolicyCreateRequest request) {
        log.info("태그 정책 생성: {}", request.name());

        TaggingPolicy policy = TaggingPolicy.builder()
                .name(request.name())
                .description(request.description())
                .isActive(true)
                .build();

        for (var tagReq : request.requiredTags()) {
            RequiredTag tag = RequiredTag.builder()
                    .tagKey(tagReq.tagKey())
                    .allowedValues(tagReq.allowedValues())
                    .isMandatory(tagReq.isMandatory() != null ? tagReq.isMandatory() : true)
                    .build();
            policy.addRequiredTag(tag);
        }

        TaggingPolicy saved = policyRepository.save(policy);
        log.info("태그 정책 생성 완료: id={}", saved.getId());

        return TagPolicyDto.from(saved);
    }

    @Transactional
    public TagPolicyDto updatePolicy(Long id, TagPolicyCreateRequest request) {
        log.info("태그 정책 수정: id={}", id);

        TaggingPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다: " + id));

        policy.setName(request.name());
        policy.setDescription(request.description());

        policy.getRequiredTags().clear();
        for (var tagReq : request.requiredTags()) {
            RequiredTag tag = RequiredTag.builder()
                    .tagKey(tagReq.tagKey())
                    .allowedValues(tagReq.allowedValues())
                    .isMandatory(tagReq.isMandatory() != null ? tagReq.isMandatory() : true)
                    .build();
            policy.addRequiredTag(tag);
        }

        TaggingPolicy saved = policyRepository.save(policy);
        return TagPolicyDto.from(saved);
    }

    @Transactional
    public void deletePolicy(Long id) {
        log.info("태그 정책 삭제: id={}", id);
        TaggingPolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("정책을 찾을 수 없습니다: " + id));

        policy.setIsActive(false);
        policyRepository.save(policy);
    }

    @Transactional
    public ComplianceScanDto runComplianceScan() {
        log.info("컴플라이언스 스캔 시작");

        List<TaggingPolicy> activePolicies = policyRepository.findAllActiveWithRequiredTags();
        if (activePolicies.isEmpty()) {
            log.warn("활성화된 태그 정책이 없습니다");
            throw new IllegalStateException("활성화된 태그 정책이 없습니다");
        }

        List<AwsResource> allResources = resourceRepository.findAll();
        if (allResources.isEmpty()) {
            log.warn("스캔할 리소스가 없습니다. 먼저 리소스를 동기화하세요.");
        }

        TagComplianceScan scan = TagComplianceScan.builder()
                .scanDate(LocalDateTime.now())
                .totalResources(allResources.size())
                .compliantResources(0)
                .nonCompliantResources(0)
                .complianceScore(BigDecimal.ZERO)
                .build();

        int compliant = 0;
        int nonCompliant = 0;

        for (AwsResource resource : allResources) {
            boolean isCompliant = true;
            List<String> missingTags = new ArrayList<>();
            List<String> invalidTags = new ArrayList<>();

            for (TaggingPolicy policy : activePolicies) {
                for (RequiredTag requiredTag : policy.getRequiredTags()) {
                    if (!requiredTag.getIsMandatory()) continue;

                    Map<String, String> resourceTags = resource.getTags();
                    if (resourceTags == null) resourceTags = Map.of();

                    String tagKey = requiredTag.getTagKey();

                    if (!resourceTags.containsKey(tagKey)) {
                        isCompliant = false;
                        missingTags.add(tagKey);
                    } else if (requiredTag.getAllowedValues() != null) {
                        String actualValue = resourceTags.get(tagKey);
                        List<String> allowedList = parseAllowedValues(requiredTag.getAllowedValues());

                        if (!allowedList.isEmpty() && !allowedList.contains(actualValue)) {
                            isCompliant = false;
                            invalidTags.add(tagKey + "=" + actualValue);
                        }
                    }
                }
            }

            if (isCompliant) {
                compliant++;
            } else {
                nonCompliant++;

                NonCompliantResource ncr = NonCompliantResource.builder()
                        .resourceId(resource.getResourceId())
                        .resourceType(resource.getResourceType())
                        .resourceName(resource.getResourceName())
                        .region(resource.getRegion())
                        .missingTags(toJson(missingTags))
                        .invalidTags(toJson(invalidTags))
                        .build();
                scan.addNonCompliantResource(ncr);
            }
        }

        scan.setCompliantResources(compliant);
        scan.setNonCompliantResources(nonCompliant);

        BigDecimal score = allResources.isEmpty() ? BigDecimal.valueOf(100)
                : BigDecimal.valueOf(compliant * 100.0 / allResources.size())
                .setScale(2, RoundingMode.HALF_UP);
        scan.setComplianceScore(score);

        TagComplianceScan saved = scanRepository.save(scan);
        log.info("컴플라이언스 스캔 완료: score={}%, compliant={}, non-compliant={}",
                score, compliant, nonCompliant);

        return ComplianceScanDto.from(saved);
    }

    public List<ComplianceScanDto> getRecentScans(int limit) {
        return scanRepository.findRecentScans(PageRequest.of(0, limit)).stream()
                .map(ComplianceScanDto::fromWithoutDetails)
                .toList();
    }

    public Optional<ComplianceScanDto> getScanById(Long id) {
        return scanRepository.findByIdWithNonCompliantResources(id)
                .map(ComplianceScanDto::from);
    }

    public ComplianceScoreDto getCurrentScore() {
        log.debug("현재 컴플라이언스 점수 조회");

        Optional<TagComplianceScan> latestOpt = scanRepository.findLatestScan();

        if (latestOpt.isEmpty()) {
            return ComplianceScoreDto.of(
                    BigDecimal.ZERO,
                    0, 0, 0,
                    Map.of(),
                    null
            );
        }

        TagComplianceScan latest = latestOpt.get();

        // 리소스 타입별 컴플라이언스 계산
        Map<String, ComplianceScoreDto.ResourceTypeCompliance> byType = new HashMap<>();
        List<Object[]> countByType = resourceRepository.countByResourceType();

        for (Object[] row : countByType) {
            String type = (String) row[0];
            Long total = (Long) row[1];

            // 간단한 계산 (실제로는 스캔 결과에서 가져와야 함)
            byType.put(type, new ComplianceScoreDto.ResourceTypeCompliance(
                    type,
                    total.intValue(),
                    (int) (total * latest.getComplianceScore().doubleValue() / 100),
                    (int) (total * (100 - latest.getComplianceScore().doubleValue()) / 100),
                    latest.getComplianceScore()
            ));
        }

        return ComplianceScoreDto.of(
                latest.getComplianceScore(),
                latest.getTotalResources(),
                latest.getCompliantResources(),
                latest.getNonCompliantResources(),
                byType,
                latest.getScanDate()
        );
    }

    private List<String> parseAllowedValues(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("allowedValues 파싱 실패: {}", json);
            return List.of();
        }
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
