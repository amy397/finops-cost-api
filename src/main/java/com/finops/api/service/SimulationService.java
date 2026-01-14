package com.finops.api.service;

import com.finops.api.dto.simulation.*;
import com.finops.api.entity.ScenarioItem;
import com.finops.api.entity.SimulationScenario;
import com.finops.api.repository.SimulationScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulationService {

    private final SimulationScenarioRepository scenarioRepository;
    private final DailyCostService dailyCostService;

    // AWS 가격 데이터 (실제로는 AWS Pricing API에서 가져와야 함)
    private static final Map<String, Map<String, BigDecimal>> PRICING_DATA = initPricingData();

    private static Map<String, Map<String, BigDecimal>> initPricingData() {
        Map<String, Map<String, BigDecimal>> data = new HashMap<>();

        // EC2 pricing (ap-northeast-2, hourly)
        Map<String, BigDecimal> ec2Pricing = new HashMap<>();
        ec2Pricing.put("t3.micro", new BigDecimal("0.0104"));
        ec2Pricing.put("t3.small", new BigDecimal("0.0208"));
        ec2Pricing.put("t3.medium", new BigDecimal("0.0416"));
        ec2Pricing.put("t3.large", new BigDecimal("0.0832"));
        ec2Pricing.put("m5.large", new BigDecimal("0.096"));
        ec2Pricing.put("m5.xlarge", new BigDecimal("0.192"));
        ec2Pricing.put("c5.large", new BigDecimal("0.085"));
        ec2Pricing.put("c5.xlarge", new BigDecimal("0.170"));
        ec2Pricing.put("r5.large", new BigDecimal("0.126"));
        ec2Pricing.put("r5.xlarge", new BigDecimal("0.252"));
        data.put("EC2", ec2Pricing);

        // RDS pricing (ap-northeast-2, hourly)
        Map<String, BigDecimal> rdsPricing = new HashMap<>();
        rdsPricing.put("db.t3.micro", new BigDecimal("0.018"));
        rdsPricing.put("db.t3.small", new BigDecimal("0.036"));
        rdsPricing.put("db.t3.medium", new BigDecimal("0.072"));
        rdsPricing.put("db.r5.large", new BigDecimal("0.250"));
        rdsPricing.put("db.r5.xlarge", new BigDecimal("0.500"));
        data.put("RDS", rdsPricing);

        // S3 pricing (per GB per month)
        Map<String, BigDecimal> s3Pricing = new HashMap<>();
        s3Pricing.put("STANDARD", new BigDecimal("0.025"));
        s3Pricing.put("INTELLIGENT_TIERING", new BigDecimal("0.023"));
        s3Pricing.put("GLACIER", new BigDecimal("0.004"));
        data.put("S3", s3Pricing);

        // Lambda pricing (per 1M requests, per GB-second)
        Map<String, BigDecimal> lambdaPricing = new HashMap<>();
        lambdaPricing.put("REQUEST", new BigDecimal("0.20"));  // per 1M requests
        lambdaPricing.put("GB_SECOND", new BigDecimal("0.0000166667"));
        data.put("LAMBDA", lambdaPricing);

        return data;
    }

    public List<ScenarioDto> getAllScenarios() {
        log.debug("전체 시뮬레이션 시나리오 조회");
        return scenarioRepository.findAllOrderByCreatedAtDesc().stream()
                .map(ScenarioDto::from)
                .toList();
    }

    public Optional<ScenarioDto> getScenarioById(Long id) {
        log.debug("시나리오 조회: {}", id);
        return scenarioRepository.findByIdWithItems(id)
                .map(ScenarioDto::from);
    }

    public CostProjectionDto calculateCost(CalculationRequest request) {
        log.info("비용 계산 시작: {} 개 항목", request.items().size());

        BigDecimal currentMonthlyCost = dailyCostService.getCurrentMonthCost();

        List<CostProjectionDto.ItemCost> itemCosts = new ArrayList<>();
        BigDecimal totalProjectedCost = currentMonthlyCost;

        for (var item : request.items()) {
            CostProjectionDto.ItemCost itemCost = calculateItemCost(item);
            itemCosts.add(itemCost);
            totalProjectedCost = totalProjectedCost.add(itemCost.monthlyCost());
        }

        return CostProjectionDto.of(currentMonthlyCost, totalProjectedCost, itemCosts);
    }

    private CostProjectionDto.ItemCost calculateItemCost(CalculationRequest.ResourceItem item) {
        String serviceCode = item.serviceCode().toUpperCase();
        String instanceType = item.instanceType() != null ? item.instanceType() : "default";
        int quantity = item.quantity() != null ? item.quantity() : 1;
        int hoursPerMonth = item.usageHoursPerMonth() != null ? item.usageHoursPerMonth() : 730;
        String region = item.region() != null ? item.region() : "ap-northeast-2";

        BigDecimal hourlyPrice = getHourlyPrice(serviceCode, instanceType);
        BigDecimal monthlyCost;
        String priceDescription;

        switch (serviceCode) {
            case "S3" -> {
                int storageGb = item.storageGb() != null ? item.storageGb() : 100;
                monthlyCost = hourlyPrice.multiply(BigDecimal.valueOf(storageGb)).multiply(BigDecimal.valueOf(quantity));
                priceDescription = String.format("$%.4f/GB/month x %dGB", hourlyPrice, storageGb);
            }
            case "LAMBDA" -> {
                // Simplified Lambda pricing
                monthlyCost = new BigDecimal("5.00").multiply(BigDecimal.valueOf(quantity));
                priceDescription = "Estimated based on average usage";
            }
            default -> {
                monthlyCost = hourlyPrice
                        .multiply(BigDecimal.valueOf(hoursPerMonth))
                        .multiply(BigDecimal.valueOf(quantity))
                        .setScale(2, RoundingMode.HALF_UP);
                priceDescription = String.format("$%.4f/hour x %d hours x %d",
                        hourlyPrice, hoursPerMonth, quantity);
            }
        }

        return new CostProjectionDto.ItemCost(
                serviceCode,
                instanceType,
                region,
                quantity,
                hourlyPrice,
                monthlyCost,
                priceDescription
        );
    }

    private BigDecimal getHourlyPrice(String serviceCode, String instanceType) {
        Map<String, BigDecimal> servicePricing = PRICING_DATA.get(serviceCode);
        if (servicePricing == null) {
            return new BigDecimal("0.10");  // Default price
        }

        BigDecimal price = servicePricing.get(instanceType);
        if (price == null) {
            // Try to find a similar type
            return servicePricing.values().stream()
                    .findFirst()
                    .orElse(new BigDecimal("0.10"));
        }

        return price;
    }

    @Transactional
    public ScenarioDto createScenario(ScenarioCreateRequest request) {
        log.info("시나리오 생성: {}", request.name());

        BigDecimal baseMonthCost = dailyCostService.getCurrentMonthCost();

        SimulationScenario scenario = SimulationScenario.builder()
                .name(request.name())
                .description(request.description())
                .baseMonthCost(baseMonthCost)
                .build();

        BigDecimal totalItemCost = BigDecimal.ZERO;

        for (var itemReq : request.items()) {
            BigDecimal itemCost = calculateItemCostFromRequest(itemReq);
            totalItemCost = totalItemCost.add(itemCost);

            ScenarioItem item = ScenarioItem.builder()
                    .actionType(itemReq.actionType() != null ? itemReq.actionType() : "ADD")
                    .serviceCode(itemReq.serviceCode())
                    .resourceType(itemReq.resourceType())
                    .instanceType(itemReq.instanceType())
                    .region(itemReq.region() != null ? itemReq.region() : "ap-northeast-2")
                    .quantity(itemReq.quantity() != null ? itemReq.quantity() : 1)
                    .usageHoursPerMonth(itemReq.usageHoursPerMonth() != null ? itemReq.usageHoursPerMonth() : 730)
                    .storageGb(itemReq.storageGb())
                    .monthlyCost(itemCost)
                    .notes(itemReq.notes())
                    .build();

            scenario.addItem(item);
        }

        BigDecimal projectedCost = baseMonthCost.add(totalItemCost);
        BigDecimal difference = projectedCost.subtract(baseMonthCost);
        BigDecimal differencePercent = baseMonthCost.compareTo(BigDecimal.ZERO) > 0
                ? difference.multiply(BigDecimal.valueOf(100)).divide(baseMonthCost, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        scenario.setProjectedMonthlyCost(projectedCost);
        scenario.setCostDifference(difference);
        scenario.setDifferencePercent(differencePercent);

        SimulationScenario saved = scenarioRepository.save(scenario);
        log.info("시나리오 저장 완료: id={}", saved.getId());

        return ScenarioDto.from(saved);
    }

    private BigDecimal calculateItemCostFromRequest(ScenarioCreateRequest.ItemRequest item) {
        String serviceCode = item.serviceCode() != null ? item.serviceCode().toUpperCase() : "EC2";
        String instanceType = item.instanceType() != null ? item.instanceType() : "t3.medium";
        int quantity = item.quantity() != null ? item.quantity() : 1;
        int hoursPerMonth = item.usageHoursPerMonth() != null ? item.usageHoursPerMonth() : 730;

        BigDecimal hourlyPrice = getHourlyPrice(serviceCode, instanceType);

        if ("S3".equals(serviceCode)) {
            int storageGb = item.storageGb() != null ? item.storageGb() : 100;
            return hourlyPrice.multiply(BigDecimal.valueOf(storageGb)).multiply(BigDecimal.valueOf(quantity));
        }

        return hourlyPrice
                .multiply(BigDecimal.valueOf(hoursPerMonth))
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void deleteScenario(Long id) {
        log.info("시나리오 삭제: id={}", id);
        scenarioRepository.deleteById(id);
    }

    public Map<String, Object> getPricingData() {
        Map<String, Object> result = new HashMap<>();

        // EC2 instance types
        result.put("ec2InstanceTypes", PRICING_DATA.get("EC2").keySet().stream().sorted().toList());

        // RDS instance types
        result.put("rdsInstanceTypes", PRICING_DATA.get("RDS").keySet().stream().sorted().toList());

        // S3 storage classes
        result.put("s3StorageClasses", PRICING_DATA.get("S3").keySet().stream().sorted().toList());

        // Regions
        result.put("regions", List.of(
                "ap-northeast-2",
                "ap-northeast-1",
                "us-east-1",
                "us-west-2",
                "eu-west-1"
        ));

        // Service codes
        result.put("serviceCodes", List.of("EC2", "RDS", "S3", "LAMBDA"));

        return result;
    }
}
