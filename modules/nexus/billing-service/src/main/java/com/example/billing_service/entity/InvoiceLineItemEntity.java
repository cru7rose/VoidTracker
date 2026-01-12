package com.example.billing_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ARCHITEKTURA: Invoice line item entity representing individual charges on an invoice.
 * Each line item represents one pricing rule application (e.g., weight charge, distance charge).
 */
@Entity
@Table(name = "billing_invoice_line_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLineItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private InvoiceEntity invoice;

    @Column(nullable = false)
    private Integer lineNumber; // Sequential line number on invoice (1, 2, 3...)

    @Column(nullable = false)
    private String description; // e.g., "Weight Charge (10.5 kg @ €2.00/kg)"

    @Column(nullable = false)
    private String itemType; // e.g., "WEIGHT", "DISTANCE", "ITEM", "SURCHARGE"

    @Column(precision = 19, scale = 2)
    private BigDecimal quantity; // e.g., 10.5 kg, 15.2 km

    private String unit; // e.g., "kg", "km", "items"

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice; // Price per unit

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice; // quantity × unitPrice (or flat rate)

    private UUID orderId; // Optional: link to specific order this line item relates to

    // Helper method to calculate total
    public void calculateTotal() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = quantity.multiply(unitPrice);
        }
        // If totalPrice is already set (flat rate), keep it
    }
}
