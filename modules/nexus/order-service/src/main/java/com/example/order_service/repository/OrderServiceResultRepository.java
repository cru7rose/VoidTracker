package com.example.order_service.repository;

import com.example.order_service.entity.OrderServiceResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA dla encji OrderServiceResultEntity.
 * Zapewnia standardowe operacje CRUD do zapisywania wyników
 * wykonanych przez kierowców usług dodatkowych.
 */
@Repository
public interface OrderServiceResultRepository extends JpaRepository<OrderServiceResultEntity, UUID> {
}