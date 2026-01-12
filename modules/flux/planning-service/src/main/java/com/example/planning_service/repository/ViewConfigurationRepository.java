package com.example.planning_service.repository;

import com.example.planning_service.entity.ViewConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ViewConfigurationRepository extends JpaRepository<ViewConfigurationEntity, UUID> {
    Optional<ViewConfigurationEntity> findByViewId(String viewId);
}
