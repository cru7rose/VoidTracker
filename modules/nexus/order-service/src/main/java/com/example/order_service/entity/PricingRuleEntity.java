package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vt_pricing_rules")
@Data
@NoArgsConstructor
public class PricingRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String conditionExpression; // SpEL expression

    @Column(nullable = false)
    private String action; // ADD_FIXED, MULTIPLY

    @Column(nullable = false)
    private BigDecimal value;
}
