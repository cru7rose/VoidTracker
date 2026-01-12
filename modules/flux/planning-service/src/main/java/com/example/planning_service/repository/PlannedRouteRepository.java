package com.example.planning_service.repository;

import com.example.planning_service.entity.PlannedRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlannedRouteRepository extends JpaRepository<PlannedRouteEntity, UUID> {
    List<PlannedRouteEntity> findAllByPlanningDate(LocalDate planningDate);
}
