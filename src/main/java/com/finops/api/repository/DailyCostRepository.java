package com.finops.api.repository;

import com.finops.api.entity.DailyCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyCostRepository extends JpaRepository<DailyCost, Long> {

    List<DailyCost> findByCostDateBetweenOrderByCostDateAsc(LocalDate startDate, LocalDate endDate);

    Optional<DailyCost> findByCostDate(LocalDate costDate);

    @Query("SELECT SUM(d.totalCost) FROM DailyCost d WHERE d.costDate BETWEEN :startDate AND :endDate")
    Optional<java.math.BigDecimal> sumTotalCostBetween(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM DailyCost d WHERE d.costDate >= :startDate ORDER BY d.costDate DESC")
    List<DailyCost> findRecentCosts(@Param("startDate") LocalDate startDate);

    boolean existsByCostDate(LocalDate costDate);
}
