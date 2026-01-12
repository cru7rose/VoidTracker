// File: planning-service/src/main/java/com/example/planning_service/repository/RoutePlanRepository.java
package com.example.planning_service.repository;

import com.example.planning_service.entity.RoutePlanEntity;
import com.example.planning_service.entity.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA dla encji RoutePlanEntity. Zapewnia
 * operacje CRUD oraz dedykowaną metodę do wyszukiwania planów
 * przeznaczonych do automatycznego uruchomienia przez scheduler.
 */
@Repository
public interface RoutePlanRepository extends JpaRepository<RoutePlanEntity, UUID> {
    List<RoutePlanEntity> findByTriggerType(TriggerType triggerType);
}