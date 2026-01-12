// File: planning-service/src/main/java/com/example/planning_service/repository/FleetVehicleRepository.java
package com.example.planning_service.repository;

import com.example.planning_service.entity.FleetVehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA dla encji FleetVehicleEntity.
 * Zapewnia dostęp do danych o pojazdach floty.
 */
@Repository
public interface FleetVehicleRepository extends JpaRepository<FleetVehicleEntity, UUID> {

    Optional<FleetVehicleEntity> findByName(String name);
}