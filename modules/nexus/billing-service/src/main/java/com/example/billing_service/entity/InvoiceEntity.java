package com.example.billing_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Invoice entity representing a billing document.
 * Stores invoice header information and links to line items for itemized breakdown.
 */
@Entity
@Table(name = "billing_invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber; // Format: INV-YYYY-MM-######

    @Column(nullable = false)
    private String clientId; // Reference to customer/organization

    private UUID orderId; // Optional: link to single order (if per-order billing)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String currency; // e.g., EUR, USD, PLN

    @Column(nullable = false)
    private LocalDate issuedDate;

    @Column(nullable = false)
    private LocalDate dueDate; // Payment due date

    private Instant paidDate;

    private String paymentReference; // External payment system reference

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceLineItemEntity> lineItems = new ArrayList<>();

    private String notes; // Additional notes or comments

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // Helper method to add line item
    public void addLineItem(InvoiceLineItemEntity lineItem) {
        lineItems.add(lineItem);
        lineItem.setInvoice(this);
    }

    // Helper method to calculate totals
    public void calculateTotals() {
        this.subtotal = lineItems.stream()
                .map(InvoiceLineItemEntity::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // TODO: Tax calculation logic (for now, assume 0% or configure per client)
        this.taxAmount = BigDecimal.ZERO;
        this.totalAmount = this.subtotal.add(this.taxAmount);
    }

    public enum InvoiceStatus {
        DRAFT,      // Invoice created but not yet sent
        ISSUED,     // Invoice sent to client
        PAID,       // Payment received
        OVERDUE,    // Payment past due date
        CANCELLED,  // Invoice cancelled
        VOID        // Invoice voided (accounting purposes)
    }
}
