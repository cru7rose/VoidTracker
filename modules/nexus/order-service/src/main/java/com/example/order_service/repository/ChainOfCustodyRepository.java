package com.example.order_service.repository;

import com.example.order_service.entity.ChainOfCustodyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChainOfCustodyRepository extends JpaRepository<ChainOfCustodyEntity, UUID> {
    Optional<ChainOfCustodyEntity> findTopByOrderIdOrderByTimestampDesc(UUID orderId);
}
