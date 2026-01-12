package com.example.order_service.repository;

import com.example.order_service.entity.OutboxEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA dla encji OutboxEventEntity.
 * Zapewnia podstawowe operacje CRUD.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    List<OutboxEventEntity> findByOrderByCreatedAtAsc(Pageable pageable);
}