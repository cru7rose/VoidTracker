package com.example.audit_service.repository;

import com.example.audit_service.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA dla encji AuditEventEntity.
 * Zapewnia standardowe operacje CRUD oraz dedykowaną metodę do wyszukiwania
 * historii zdarzeń dla konkretnej encji (np. dla danego zlecenia),
 * posortowanych chronologicznie.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {
    List<AuditEventEntity> findByEntityTypeAndEntityIdOrderByTimestampAsc(String entityType, String entityId);
}