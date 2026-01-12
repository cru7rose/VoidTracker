// File: analytics-service/src/main/java/com/example/analytics_service/repository/AnalyticsOrderRepository.java
package com.example.analytics_service.repository;

import com.example.analytics_service.entity.AnalyticsOrderEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA rozszerzone o metodę `findAllByOrderIdIn`,
 * która jest kluczowa dla wydajnej implementacji nowego endpointu batchowego.
 * Pozwala na pobranie danych dla wielu zleceń w jednym zapytaniu SQL.
 */
@Repository
public interface AnalyticsOrderRepository extends JpaRepository<AnalyticsOrderEntry, UUID> {
    List<AnalyticsOrderEntry> findAllByOrderIdIn(List<UUID> orderIds);
}