package com.example.planning_service.repository;

import com.example.planning_service.entity.OrderTileBindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderTileBindingRepository extends JpaRepository<OrderTileBindingEntity, UUID> {
    List<OrderTileBindingEntity> findByOrderId(UUID orderId);

    void deleteByOrderIdAndTagId(UUID orderId, UUID tagId);
}
