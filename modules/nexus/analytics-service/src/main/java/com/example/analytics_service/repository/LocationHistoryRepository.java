package com.example.analytics_service.repository;

import com.example.analytics_service.entity.LocationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA dla nowej encji `LocationHistoryEntity`.
 * Udostępnia dedykowaną metodę do wyszukiwania wszystkich historycznych
 * punktów lokalizacyjnych dla danego zlecenia, posortowanych chronologicznie.
 */
@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistoryEntity, Long> {

    List<LocationHistoryEntity> findByOrderIdOrderByTimestampAsc(UUID orderId);
}