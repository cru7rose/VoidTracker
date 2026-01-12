package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vt_rate_cards")
public class RateCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String sourceZone; // e.g., "00" (Zip prefix)

    @Column(nullable = false)
    private String destinationZone; // e.g., "01"

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private String currency;

    private String tenantId;

    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSourceZone() {
        return sourceZone;
    }

    public void setSourceZone(String sourceZone) {
        this.sourceZone = sourceZone;
    }

    public String getDestinationZone() {
        return destinationZone;
    }

    public void setDestinationZone(String destinationZone) {
        this.destinationZone = destinationZone;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
