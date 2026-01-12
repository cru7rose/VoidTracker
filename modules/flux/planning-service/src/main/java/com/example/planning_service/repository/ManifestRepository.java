package com.example.planning_service.repository;

import com.example.planning_service.entity.ManifestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface ManifestRepository extends JpaRepository<ManifestEntity, UUID> {
    List<ManifestEntity> findByDriverIdAndDate(UUID driverId, LocalDate date);

    List<ManifestEntity> findByDate(LocalDate date);

    List<ManifestEntity> findByStatus(com.example.planning_service.entity.ManifestStatus status);

    List<ManifestEntity> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<ManifestEntity> findByDriverIdAndDateBetween(UUID driverId, LocalDate startDate, LocalDate endDate);
}
