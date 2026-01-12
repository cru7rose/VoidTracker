package com.example.order_service.repository;

import com.example.order_service.entity.ePoDEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA dla encji ePoDEntity.
 * Sygnatura metody findByOrderId została zaktualizowana do używania typu UUID.
 */
@Repository
public interface ePoDRepository extends JpaRepository<ePoDEntity, UUID> {
    List<ePoDEntity> findByOrderId(UUID orderId); // ZMIANA: String -> UUID
}