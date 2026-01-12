package com.example.billing_service.repository;

import com.example.billing_service.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
    
    Optional<InvoiceEntity> findByInvoiceNumber(String invoiceNumber);
    
    List<InvoiceEntity> findByClientId(String clientId);
    
    List<InvoiceEntity> findByClientIdAndStatus(String clientId, InvoiceEntity.InvoiceStatus status);
    
    Optional<InvoiceEntity> findByOrderId(UUID orderId);
    
    List<InvoiceEntity> findByIssuedDateBetween(LocalDate startDate, LocalDate endDate);
    
    long countByIssuedDateAndClientId(LocalDate issuedDate, String clientId);
}
