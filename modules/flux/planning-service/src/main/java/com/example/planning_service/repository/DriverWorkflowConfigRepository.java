package com.example.planning_service.repository;

import com.example.planning_service.entity.DriverWorkflowConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverWorkflowConfigRepository extends JpaRepository<DriverWorkflowConfigEntity, UUID> {
    Optional<DriverWorkflowConfigEntity> findByConfigCode(String configCode);
}
