package com.example.planning_service.repository;

import com.example.planning_service.entity.WebhookConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookConfigRepository extends JpaRepository<WebhookConfigEntity, UUID> {
    Optional<WebhookConfigEntity> findByEventType(String eventType);
}
