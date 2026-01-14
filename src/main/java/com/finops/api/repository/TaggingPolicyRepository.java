package com.finops.api.repository;

import com.finops.api.entity.TaggingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaggingPolicyRepository extends JpaRepository<TaggingPolicy, Long> {

    List<TaggingPolicy> findByIsActiveTrue();

    @Query("SELECT p FROM TaggingPolicy p LEFT JOIN FETCH p.requiredTags WHERE p.id = :id")
    TaggingPolicy findByIdWithRequiredTags(@Param("id") Long id);

    @Query("SELECT p FROM TaggingPolicy p LEFT JOIN FETCH p.requiredTags WHERE p.isActive = true")
    List<TaggingPolicy> findAllActiveWithRequiredTags();
}
