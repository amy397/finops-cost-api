package com.finops.api.repository;

import com.finops.api.entity.AwsResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AwsResourceRepository extends JpaRepository<AwsResource, Long> {

    Optional<AwsResource> findByResourceId(String resourceId);

    List<AwsResource> findByResourceType(String resourceType);

    Page<AwsResource> findByResourceType(String resourceType, Pageable pageable);

    List<AwsResource> findByRegion(String region);

    List<AwsResource> findByState(String state);

    @Query("SELECT r FROM AwsResource r WHERE r.resourceType = :type AND r.region = :region")
    List<AwsResource> findByResourceTypeAndRegion(
            @Param("type") String resourceType,
            @Param("region") String region
    );

    @Query("SELECT r.resourceType, COUNT(r) FROM AwsResource r GROUP BY r.resourceType")
    List<Object[]> countByResourceType();

    @Query("SELECT r.region, COUNT(r) FROM AwsResource r GROUP BY r.region")
    List<Object[]> countByRegion();

    @Query("SELECT r FROM AwsResource r WHERE r.state = 'stopped' OR r.state = 'unused'")
    List<AwsResource> findIdleResources();

    boolean existsByResourceId(String resourceId);

    void deleteByResourceId(String resourceId);
}
