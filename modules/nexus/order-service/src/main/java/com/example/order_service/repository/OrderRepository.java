package com.example.order_service.repository;

import com.example.order_service.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {
    Optional<OrderEntity> findByExternalId(String externalId);

    // Legacy support
    List<OrderEntity> findByStatus(com.example.danxils_commons.enums.OrderStatus status);
}