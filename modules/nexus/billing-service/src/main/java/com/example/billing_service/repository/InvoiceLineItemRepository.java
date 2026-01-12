package com.example.billing_service.repository;

import com.example.billing_service.entity.InvoiceLineItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItemEntity, UUID> {
    
    List<InvoiceLineItemEntity> findByInvoiceId(UUID invoiceId);
    
    List<InvoiceLineItemEntity> findByOrderId(UUID orderId);
}
