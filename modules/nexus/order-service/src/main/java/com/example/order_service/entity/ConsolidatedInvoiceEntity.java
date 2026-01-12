package com.example.order_service.entity;

import com.example.order_service.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vt_consolidated_invoices")
@Data
@NoArgsConstructor
public class ConsolidatedInvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID organizationId; // Links to IAM Organization

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private String monthYear; // e.g., "2025-11"

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String currency;

    private String pdfUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(nullable = false)
    private Instant issuedAt;

    private Instant dueDate;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String departmentBreakdown; // JSON: {"DeptA": 100.00, "DeptB": 200.00}
}
