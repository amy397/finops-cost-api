package com.finops.api.service;

import com.finops.api.dto.CostSummaryDto;
import com.finops.api.dto.ServiceCostSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final DailyCostService dailyCostService;
    private final ServiceCostService serviceCostService;

    private static final int TOP_SERVICES_LIMIT = 5;

    public CostSummaryDto getCostSummary() {
        log.debug("대시보드 요약 정보 조회");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate startOfMonth = today.withDayOfMonth(1);

        // 이번 달 비용
        BigDecimal currentMonthCost = dailyCostService.getCurrentMonthCost();

        // 지난 달 비용
        BigDecimal previousMonthCost = dailyCostService.getPreviousMonthCost();

        // 오늘/어제 비용
        BigDecimal todayCost = dailyCostService.getTotalCostBetween(today, today);
        BigDecimal yesterdayCost = dailyCostService.getTotalCostBetween(yesterday, yesterday);

        // Top 5 서비스
        List<ServiceCostSummaryDto> topServices = serviceCostService.getTopServices(startOfMonth, today, TOP_SERVICES_LIMIT);

        return CostSummaryDto.of(
                currentMonthCost,
                previousMonthCost,
                todayCost,
                yesterdayCost,
                topServices
        );
    }
}
