package com.finops.api.repository;

import com.finops.api.entity.TagComplianceScan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagComplianceScanRepository extends JpaRepository<TagComplianceScan, Long> {

    @Query("SELECT s FROM TagComplianceScan s ORDER BY s.scanDate DESC")
    List<TagComplianceScan> findRecentScans(Pageable pageable);

    @Query("SELECT s FROM TagComplianceScan s ORDER BY s.scanDate DESC LIMIT 1")
    Optional<TagComplianceScan> findLatestScan();

    @Query("SELECT s FROM TagComplianceScan s LEFT JOIN FETCH s.nonCompliantResourceList WHERE s.id = :id")
    Optional<TagComplianceScan> findByIdWithNonCompliantResources(Long id);
}
