package com.example.billing_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ARCHITEKTURA: Detailed breakdown of pricing calculation.
 * Shows exactly how the price was computed, which rules applied, and subtotals.
 */
@Data
@Builder
public class CalculationBreakdownDto {
    
    private BigDecimal totalAmount;
    private String currency;
    private List<AppliedRuleDto> appliedRules;
    private List<String> warnings; // e.g., "No distance provided, distance charges skipped"
    
    @Data
    @Builder
    public static class AppliedRuleDto {
        private String ruleName;
        private String ruleType; // WEIGHT, DISTANCE, ITEM, etc.
        private BigDecimal metricValue;
        private String unit;
        private BigDecimal basePrice;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String calculation; // e.g., "€30.00 + (10.5 kg × €2.00/kg) = €51.00"
    }
    
    /**
     * Generate human-readable summary.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total: %s %s\n", totalAmount, currency));
        sb.append(String.format("Applied %d pricing rules:\n", appliedRules.size()));
        
        for (AppliedRuleDto rule : appliedRules) {
            sb.append(String.format("  - %s: %s = %s %s\n", 
                    rule.getRuleName(), 
                    rule.getCalculation(), 
                    rule.getSubtotal(), 
                    currency));
        }
        
        if (warnings != null && !warnings.isEmpty()) {
            sb.append("\nWarnings:\n");
            warnings.forEach(w -> sb.append("  - ").append(w).append("\n"));
        }
        
        return sb.toString();
    }
}
