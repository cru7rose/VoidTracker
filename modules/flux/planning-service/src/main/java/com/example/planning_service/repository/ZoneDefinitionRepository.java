package com.example.planning_service.repository;

import com.example.planning_service.entity.ZoneDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZoneDefinitionRepository extends JpaRepository<ZoneDefinitionEntity, UUID> {
    Optional<ZoneDefinitionEntity> findByCode(String code);
}
