package com.example.planning_service.repository;

import com.example.planning_service.entity.OptimizationSolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptimizationSolutionRepository extends JpaRepository<OptimizationSolutionEntity, UUID> {
    Optional<OptimizationSolutionEntity> findTopByOrderByCreatedAtDesc();
}
