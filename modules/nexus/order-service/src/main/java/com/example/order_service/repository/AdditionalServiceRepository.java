package com.example.order_service.repository;

import com.example.order_service.entity.AdditionalServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA dla słownika usług dodatkowych.
 * Zapewnia standardowe operacje CRUD oraz dedykowaną metodę do wydajnego
 * wyszukiwania wielu usług na podstawie ich kodów.
 */
@Repository
public interface AdditionalServiceRepository extends JpaRepository<AdditionalServiceEntity, UUID> {
    List<AdditionalServiceEntity> findAllByServiceCodeIn(List<String> serviceCodes);
}