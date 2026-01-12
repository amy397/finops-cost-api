package com.finops.api.service;

import com.finops.api.dto.ServiceCostDto;
import com.finops.api.dto.ServiceCostSummaryDto;
import com.finops.api.repository.ServiceCostRepository;
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
public class ServiceCostService {

    private final ServiceCostRepository serviceCostRepository;

    public List<ServiceCostDto> getServiceCosts(LocalDate startDate, LocalDate endDate) {
        log.debug("서비스별 비용 조회: {} ~ {}", startDate, endDate);

        return serviceCostRepository.findByCostDateBetweenOrderByServiceNameAsc(startDate, endDate)
                .stream()
                .map(ServiceCostDto::from)
                .toList();
    }

    public List<ServiceCostSummaryDto> getServiceCostSummary(LocalDate startDate, LocalDate endDate) {
        var results = serviceCostRepository.findServiceCostSummary(startDate, endDate);

        BigDecimal grandTotal = results.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return results.stream()
                .map(row -> ServiceCostSummaryDto.of(
                        (String) row[0],
                        (BigDecimal) row[1],
                        grandTotal
                ))
                .toList();
    }

    public List<ServiceCostSummaryDto> getTopServices(LocalDate startDate, LocalDate endDate, int limit) {
        var results = serviceCostRepository.findTopServices(startDate, endDate, limit);

        BigDecimal grandTotal = results.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return results.stream()
                .map(row -> ServiceCostSummaryDto.of(
                        (String) row[0],
                        (BigDecimal) row[1],
                        grandTotal
                ))
                .toList();
    }
}
