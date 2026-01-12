package com.example.order_service.repository;

import com.example.order_service.entity.HubEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HubRepository extends JpaRepository<HubEntity, UUID> {
    Optional<HubEntity> findByLocationCode(String locationCode);
}
