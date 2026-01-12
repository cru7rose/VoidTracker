package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vt_rate_card_rules")
public class RateCardRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID rateCardId;

    @Column(nullable = false)
    private String attributeKey; // e.g., "assetType", "weight"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleOperator operator; // EQUALS, GT, LT, CONTAINS

    @Column(nullable = false)
    private String compareValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModifierType modifierType; // FIXED_ADD, PERCENTAGE_ADD, MULTIPLIER

    @Column(nullable = false)
    private BigDecimal modifierValue;

    public enum RuleOperator {
        EQUALS, GT, LT, CONTAINS
    }

    public enum ModifierType {
        FIXED_ADD, PERCENTAGE_ADD, MULTIPLIER
    }

    public RateCardRuleEntity() {
    }

    public RateCardRuleEntity(UUID id, UUID rateCardId, String attributeKey, RuleOperator operator, String compareValue,
            ModifierType modifierType, BigDecimal modifierValue) {
        this.id = id;
        this.rateCardId = rateCardId;
        this.attributeKey = attributeKey;
        this.operator = operator;
        this.compareValue = compareValue;
        this.modifierType = modifierType;
        this.modifierValue = modifierValue;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRateCardId() {
        return rateCardId;
    }

    public void setRateCardId(UUID rateCardId) {
        this.rateCardId = rateCardId;
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    public void setAttributeKey(String attributeKey) {
        this.attributeKey = attributeKey;
    }

    public RuleOperator getOperator() {
        return operator;
    }

    public void setOperator(RuleOperator operator) {
        this.operator = operator;
    }

    public String getCompareValue() {
        return compareValue;
    }

    public void setCompareValue(String compareValue) {
        this.compareValue = compareValue;
    }

    public ModifierType getModifierType() {
        return modifierType;
    }

    public void setModifierType(ModifierType modifierType) {
        this.modifierType = modifierType;
    }

    public BigDecimal getModifierValue() {
        return modifierValue;
    }

    public void setModifierValue(BigDecimal modifierValue) {
        this.modifierValue = modifierValue;
    }
}
