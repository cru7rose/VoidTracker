package com.example.order_service.repository;

import com.example.order_service.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
    boolean existsByOrderId(UUID orderId);
}
