package com.example.planning_service.repository;

import com.example.planning_service.entity.DriverSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverSessionRepository extends JpaRepository<DriverSessionEntity, UUID> {
    Optional<DriverSessionEntity> findByToken(String token);
}
