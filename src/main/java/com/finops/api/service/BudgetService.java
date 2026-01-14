package com.finops.api.service;

import com.finops.api.dto.budget.*;
import com.finops.api.entity.Budget;
import com.finops.api.entity.BudgetAlert;
import com.finops.api.entity.BudgetThreshold;
import com.finops.api.repository.BudgetAlertRepository;
import com.finops.api.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetAlertRepository budgetAlertRepository;
    private final DailyCostService dailyCostService;

    public List<BudgetDto> getAllBudgets() {
        log.debug("전체 예산 조회");
        return budgetRepository.findByIsActiveTrue().stream()
                .map(BudgetDto::from)
                .toList();
    }

    public Optional<BudgetDto> getBudgetById(Long id) {
        log.debug("예산 조회: {}", id);
        return Optional.ofNullable(budgetRepository.findByIdWithThresholds(id))
                .map(BudgetDto::from);
    }

    @Transactional
    public BudgetDto createBudget(BudgetCreateRequest request) {
        log.info("예산 생성: {}", request.name());

        Budget budget = Budget.builder()
                .name(request.name())
                .budgetType(request.budgetType())
                .targetId(request.targetId())
                .amount(request.amount())
                .periodType(request.periodType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .currency(request.currency() != null ? request.currency() : "USD")
                .description(request.description())
                .isActive(true)
                .build();

        if (request.thresholds() != null) {
            for (var thresholdReq : request.thresholds()) {
                BudgetThreshold threshold = BudgetThreshold.builder()
                        .thresholdPercent(thresholdReq.thresholdPercent())
                        .notificationType(thresholdReq.notificationType() != null ? thresholdReq.notificationType() : "SLACK")
                        .isActive(true)
                        .build();
                budget.addThreshold(threshold);
            }
        } else {
            // 기본 임계값 추가
            addDefaultThresholds(budget);
        }

        Budget saved = budgetRepository.save(budget);
        log.info("예산 생성 완료: id={}", saved.getId());

        return BudgetDto.from(saved);
    }

    private void addDefaultThresholds(Budget budget) {
        int[] defaults = {50, 80, 100};
        for (int percent : defaults) {
            BudgetThreshold threshold = BudgetThreshold.builder()
                    .thresholdPercent(percent)
                    .notificationType("SLACK")
                    .isActive(true)
                    .build();
            budget.addThreshold(threshold);
        }
    }

    @Transactional
    public BudgetDto updateBudget(Long id, BudgetCreateRequest request) {
        log.info("예산 수정: id={}", id);

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예산을 찾을 수 없습니다: " + id));

        budget.setName(request.name());
        budget.setBudgetType(request.budgetType());
        budget.setTargetId(request.targetId());
        budget.setAmount(request.amount());
        budget.setPeriodType(request.periodType());
        budget.setStartDate(request.startDate());
        budget.setEndDate(request.endDate());
        if (request.currency() != null) {
            budget.setCurrency(request.currency());
        }
        budget.setDescription(request.description());

        // 기존 임계값 제거 후 새로 추가
        if (request.thresholds() != null && !request.thresholds().isEmpty()) {
            budget.getThresholds().clear();
            for (var thresholdReq : request.thresholds()) {
                BudgetThreshold threshold = BudgetThreshold.builder()
                        .thresholdPercent(thresholdReq.thresholdPercent())
                        .notificationType(thresholdReq.notificationType() != null ? thresholdReq.notificationType() : "SLACK")
                        .isActive(true)
                        .build();
                budget.addThreshold(threshold);
            }
        }

        Budget saved = budgetRepository.save(budget);
        return BudgetDto.from(saved);
    }

    @Transactional
    public void deleteBudget(Long id) {
        log.info("예산 삭제: id={}", id);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예산을 찾을 수 없습니다: " + id));

        budget.setIsActive(false);
        budgetRepository.save(budget);
    }

    public BudgetUsageDto getBudgetUsage(Long id) {
        log.debug("예산 사용량 조회: id={}", id);

        Budget budget = budgetRepository.findByIdWithThresholds(id);
        if (budget == null) {
            throw new IllegalArgumentException("예산을 찾을 수 없습니다: " + id);
        }

        return calculateBudgetUsage(budget);
    }

    public BudgetDashboardDto getBudgetDashboard() {
        log.debug("예산 대시보드 조회");

        List<Budget> activeBudgets = budgetRepository.findAllActiveWithThresholds();
        List<BudgetUsageDto> usages = activeBudgets.stream()
                .map(this::calculateBudgetUsage)
                .toList();

        return BudgetDashboardDto.of(usages);
    }

    private BudgetUsageDto calculateBudgetUsage(Budget budget) {
        LocalDate periodStart;
        LocalDate periodEnd;

        LocalDate today = LocalDate.now();

        switch (budget.getPeriodType()) {
            case "MONTHLY" -> {
                periodStart = today.withDayOfMonth(1);
                periodEnd = today.withDayOfMonth(today.lengthOfMonth());
            }
            case "QUARTERLY" -> {
                int quarter = (today.getMonthValue() - 1) / 3;
                periodStart = today.withMonth(quarter * 3 + 1).withDayOfMonth(1);
                periodEnd = periodStart.plusMonths(3).minusDays(1);
            }
            case "YEARLY" -> {
                periodStart = today.withDayOfYear(1);
                periodEnd = today.withDayOfYear(today.lengthOfYear());
            }
            default -> {
                periodStart = budget.getStartDate();
                periodEnd = budget.getEndDate() != null ? budget.getEndDate() : today;
            }
        }

        // 실제 비용 조회
        BigDecimal actualAmount = dailyCostService.getTotalCostBetween(periodStart, today);

        // 임계값 상태 계산
        List<BudgetUsageDto.ThresholdStatus> thresholdStatuses = new ArrayList<>();
        for (BudgetThreshold threshold : budget.getThresholds()) {
            BigDecimal triggerAmount = budget.getAmount()
                    .multiply(BigDecimal.valueOf(threshold.getThresholdPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            boolean triggered = actualAmount.compareTo(triggerAmount) >= 0;

            thresholdStatuses.add(new BudgetUsageDto.ThresholdStatus(
                    threshold.getThresholdPercent(),
                    triggered,
                    triggerAmount
            ));
        }

        return BudgetUsageDto.of(
                budget.getId(),
                budget.getName(),
                budget.getAmount(),
                actualAmount,
                periodStart,
                periodEnd,
                thresholdStatuses
        );
    }

    public List<BudgetAlert> getBudgetAlerts(Long budgetId) {
        log.debug("예산 알림 이력 조회: budgetId={}", budgetId);
        return budgetAlertRepository.findByBudgetIdOrderBySentAtDesc(budgetId);
    }

    @Transactional
    public void checkBudgetThresholds() {
        log.info("예산 임계값 체크 시작");

        List<Budget> activeBudgets = budgetRepository.findAllActiveWithThresholds();

        for (Budget budget : activeBudgets) {
            BudgetUsageDto usage = calculateBudgetUsage(budget);

            for (BudgetThreshold threshold : budget.getThresholds()) {
                if (!threshold.getIsActive()) continue;

                BigDecimal triggerPercent = BigDecimal.valueOf(threshold.getThresholdPercent());

                if (usage.usagePercent().compareTo(triggerPercent) >= 0) {
                    // 24시간 이내에 같은 알림이 이미 발송되었는지 체크
                    long recentAlerts = budgetAlertRepository.countAlertsByBudgetAndThreshold(
                            budget.getId(),
                            threshold.getThresholdPercent(),
                            LocalDateTime.now().minusHours(24)
                    );

                    if (recentAlerts == 0) {
                        createBudgetAlert(budget, threshold, usage);
                    }
                }
            }
        }

        log.info("예산 임계값 체크 완료");
    }

    @Transactional
    public void createBudgetAlert(Budget budget, BudgetThreshold threshold, BudgetUsageDto usage) {
        log.info("예산 알림 생성: budget={}, threshold={}%", budget.getName(), threshold.getThresholdPercent());

        String message = String.format(
                "[FinOps 예산 알림] %s 예산이 %d%% 임계값을 초과했습니다. 현재 사용률: %.2f%%, 사용금액: $%.2f / $%.2f",
                budget.getName(),
                threshold.getThresholdPercent(),
                usage.usagePercent(),
                usage.actualAmount(),
                usage.budgetAmount()
        );

        BudgetAlert alert = BudgetAlert.builder()
                .budget(budget)
                .thresholdPercent(threshold.getThresholdPercent())
                .actualAmount(usage.actualAmount())
                .budgetAmount(usage.budgetAmount())
                .usagePercent(usage.usagePercent())
                .alertMessage(message)
                .slackSent(false)
                .emailSent(false)
                .build();

        budgetAlertRepository.save(alert);

        // Threshold의 마지막 트리거 시간 업데이트
        threshold.setLastTriggeredAt(LocalDateTime.now());

        log.info("예산 알림 저장 완료: alertId={}", alert.getId());
    }
}
