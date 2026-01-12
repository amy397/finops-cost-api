package com.finops.api.repository;

import com.finops.api.entity.ServiceCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ServiceCostRepository extends JpaRepository<ServiceCost, Long> {

    List<ServiceCost> findByCostDateBetweenOrderByServiceNameAsc(LocalDate startDate, LocalDate endDate);

    List<ServiceCost> findByCostDate(LocalDate costDate);

    @Query("""
           SELECT s.serviceName, SUM(s.cost) as totalCost
           FROM ServiceCost s
           WHERE s.costDate BETWEEN :startDate AND :endDate
           GROUP BY s.serviceName
           ORDER BY totalCost DESC
           """)
    List<Object[]> findServiceCostSummary(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("""
           SELECT s.serviceName, SUM(s.cost)
           FROM ServiceCost s
           WHERE s.costDate BETWEEN :startDate AND :endDate
           GROUP BY s.serviceName
           ORDER BY SUM(s.cost) DESC
           LIMIT :limit
           """)
    List<Object[]> findTopServices(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   @Param("limit") int limit);
}
