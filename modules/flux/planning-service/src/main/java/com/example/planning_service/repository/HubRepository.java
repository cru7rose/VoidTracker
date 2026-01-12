package com.example.planning_service.repository;

import com.example.planning_service.entity.HubEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HubRepository extends JpaRepository<HubEntity, UUID> {
    Optional<HubEntity> findByHubCode(String hubCode);

    boolean existsByHubCode(String hubCode);
}
