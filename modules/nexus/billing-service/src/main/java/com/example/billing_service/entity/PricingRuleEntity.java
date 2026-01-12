package com.example.billing_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "billing_pricing_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "rate_card_id", nullable = false)
    private RateCardEntity rateCard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metric; // WEIGHT, VOLUME, COUNT, DISTANCE

    // Range Logic (e.g., 0 to 10 kg)
    private BigDecimal rangeStart;
    private BigDecimal rangeEnd; // null means Infinity

    // Pricing Logic
    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO; // Price per 1 unit of Metric above rangeStart

    public enum MetricType {
        WEIGHT, VOLUME, COUNT, DISTANCE, ITEM
    }
}
