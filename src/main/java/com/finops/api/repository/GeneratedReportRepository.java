package com.finops.api.repository;

import com.finops.api.entity.GeneratedReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {

    @Query("SELECT r FROM GeneratedReport r ORDER BY r.generatedAt DESC")
    List<GeneratedReport> findRecentReports(Pageable pageable);

    List<GeneratedReport> findByGenerationStatus(String status);

    @Query("SELECT r FROM GeneratedReport r WHERE r.expiresAt < :now")
    List<GeneratedReport> findExpiredReports(LocalDateTime now);

    @Query("SELECT r FROM GeneratedReport r WHERE r.emailStatus = 'PENDING' ORDER BY r.generatedAt ASC")
    List<GeneratedReport> findPendingEmailReports();
}
