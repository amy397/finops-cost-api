package com.finops.api.repository;

import com.finops.api.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByIsActiveTrue();

    List<Budget> findByBudgetType(String budgetType);

    List<Budget> findByTargetId(String targetId);

    @Query("SELECT b FROM Budget b WHERE b.isActive = true AND b.startDate <= :date AND (b.endDate IS NULL OR b.endDate >= :date)")
    List<Budget> findActiveBudgetsForDate(@Param("date") LocalDate date);

    @Query("SELECT b FROM Budget b WHERE b.isActive = true AND b.periodType = :periodType")
    List<Budget> findByPeriodType(@Param("periodType") String periodType);

    @Query("SELECT b FROM Budget b LEFT JOIN FETCH b.thresholds WHERE b.id = :id")
    Budget findByIdWithThresholds(@Param("id") Long id);

    @Query("SELECT b FROM Budget b LEFT JOIN FETCH b.thresholds WHERE b.isActive = true")
    List<Budget> findAllActiveWithThresholds();
}
