package com.example.order_service.repository;

import com.example.order_service.entity.RateCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateCardRepository extends JpaRepository<RateCardEntity, UUID> {
    Optional<RateCardEntity> findBySourceZoneAndDestinationZone(String sourceZone, String destinationZone);
}
