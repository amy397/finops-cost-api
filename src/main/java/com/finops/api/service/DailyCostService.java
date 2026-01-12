package com.finops.api.service;

import com.finops.api.dto.DailyCostDto;
import com.finops.api.repository.DailyCostRepository;
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
public class DailyCostService {

    private final DailyCostRepository dailyCostRepository;

    public List<DailyCostDto> getDailyCosts(LocalDate startDate, LocalDate endDate) {
        log.debug("일별 비용 조회: {} ~ {}", startDate, endDate);

        return dailyCostRepository.findByCostDateBetweenOrderByCostDateAsc(startDate, endDate)
                .stream()
                .map(DailyCostDto::from)
                .toList();
    }

    public DailyCostDto getTodayCost() {
        return dailyCostRepository.findByCostDate(LocalDate.now())
                .map(DailyCostDto::from)
                .orElse(null);
    }

    public BigDecimal getTotalCostBetween(LocalDate startDate, LocalDate endDate) {
        return dailyCostRepository.sumTotalCostBetween(startDate, endDate)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getCurrentMonthCost() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        return getTotalCostBetween(startOfMonth, now);
    }

    public BigDecimal getPreviousMonthCost() {
        LocalDate now = LocalDate.now();
        LocalDate startOfPrevMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfPrevMonth = now.withDayOfMonth(1).minusDays(1);
        return getTotalCostBetween(startOfPrevMonth, endOfPrevMonth);
    }
}
