package com.finops.api.repository;

import com.finops.api.entity.SimulationScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationScenarioRepository extends JpaRepository<SimulationScenario, Long> {

    @Query("SELECT s FROM SimulationScenario s ORDER BY s.createdAt DESC")
    List<SimulationScenario> findAllOrderByCreatedAtDesc();

    @Query("SELECT s FROM SimulationScenario s LEFT JOIN FETCH s.items WHERE s.id = :id")
    Optional<SimulationScenario> findByIdWithItems(@Param("id") Long id);

    List<SimulationScenario> findByCreatedBy(String createdBy);
}
