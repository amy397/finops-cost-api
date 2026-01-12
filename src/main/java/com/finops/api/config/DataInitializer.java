package com.finops.api.config;

import com.finops.api.entity.DailyCost;
import com.finops.api.entity.ServiceCost;
import com.finops.api.repository.DailyCostRepository;
import com.finops.api.repository.ServiceCostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DailyCostRepository dailyCostRepository;
    private final ServiceCostRepository serviceCostRepository;

    private static final List<String> AWS_SERVICES = List.of(
            "Amazon EC2", "Amazon S3", "Amazon RDS",
            "AWS Lambda", "Amazon CloudFront", "Amazon DynamoDB",
            "Amazon EKS", "AWS Fargate", "Amazon ElastiCache"
    );

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        if (dailyCostRepository.count() > 0) {
            log.info("데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("테스트 데이터 초기화 시작...");
        initializeDailyCosts();
        initializeServiceCosts();
        log.info("테스트 데이터 초기화 완료!");
    }

    private void initializeDailyCosts() {
        LocalDate today = LocalDate.now();

        for (int i = 60; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal baseCost = BigDecimal.valueOf(100 + random.nextDouble() * 50);

            // 주말은 비용이 적음
            if (date.getDayOfWeek().getValue() >= 6) {
                baseCost = baseCost.multiply(BigDecimal.valueOf(0.7));
            }

            DailyCost dailyCost = DailyCost.builder()
                    .costDate(date)
                    .totalCost(baseCost.setScale(4, java.math.RoundingMode.HALF_UP))
                    .currency("USD")
                    .build();

            dailyCostRepository.save(dailyCost);
        }

        log.info("일별 비용 데이터 {} 건 생성", dailyCostRepository.count());
    }

    private void initializeServiceCosts() {
        LocalDate today = LocalDate.now();

        for (int i = 60; i >= 0; i--) {
            LocalDate date = today.minusDays(i);

            for (String service : AWS_SERVICES) {
                BigDecimal cost = generateServiceCost(service);

                ServiceCost serviceCost = ServiceCost.builder()
                        .costDate(date)
                        .serviceName(service)
                        .cost(cost.setScale(4, java.math.RoundingMode.HALF_UP))
                        .currency("USD")
                        .build();

                serviceCostRepository.save(serviceCost);
            }
        }

        log.info("서비스별 비용 데이터 {} 건 생성", serviceCostRepository.count());
    }

    private BigDecimal generateServiceCost(String service) {
        double baseCost = switch (service) {
            case "Amazon EC2" -> 30 + random.nextDouble() * 20;
            case "Amazon RDS" -> 20 + random.nextDouble() * 15;
            case "Amazon S3" -> 10 + random.nextDouble() * 10;
            case "Amazon EKS" -> 15 + random.nextDouble() * 10;
            case "AWS Lambda" -> 5 + random.nextDouble() * 5;
            default -> 3 + random.nextDouble() * 7;
        };
        return BigDecimal.valueOf(baseCost);
    }
}
