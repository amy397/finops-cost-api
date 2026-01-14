package com.finops.api.repository;

import com.finops.api.entity.BudgetAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {

    List<BudgetAlert> findByBudgetIdOrderBySentAtDesc(Long budgetId);

    @Query("SELECT ba FROM BudgetAlert ba WHERE ba.budget.id = :budgetId AND ba.sentAt >= :since ORDER BY ba.sentAt DESC")
    List<BudgetAlert> findRecentAlertsByBudget(
            @Param("budgetId") Long budgetId,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT ba FROM BudgetAlert ba WHERE ba.sentAt >= :since ORDER BY ba.sentAt DESC")
    List<BudgetAlert> findRecentAlerts(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(ba) FROM BudgetAlert ba WHERE ba.budget.id = :budgetId AND ba.thresholdPercent = :threshold AND ba.sentAt >= :since")
    long countAlertsByBudgetAndThreshold(
            @Param("budgetId") Long budgetId,
            @Param("threshold") Integer threshold,
            @Param("since") LocalDateTime since
    );
}
