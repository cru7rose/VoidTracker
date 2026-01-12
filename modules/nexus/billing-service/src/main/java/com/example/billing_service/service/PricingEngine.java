package com.example.billing_service.service;

import com.example.billing_service.dto.CalculationBreakdownDto;
import com.example.billing_service.dto.OrderContextDto;
import com.example.billing_service.entity.PricingRuleEntity;
import com.example.billing_service.entity.RateCardEntity;
import com.example.billing_service.exception.InvalidOrderContextException;
import com.example.billing_service.exception.RateCardNotFoundException;
import com.example.billing_service.repository.PricingRuleRepository;
import com.example.billing_service.repository.RateCardRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PricingEngine {

    private final RateCardRepository rateCardRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final Timer pricingTimer;
    private final Counter successCounter;
    private final Counter errorCounter;

    public PricingEngine(RateCardRepository rateCardRepository,
                        PricingRuleRepository pricingRuleRepository,
                        MeterRegistry meterRegistry) {
        this.rateCardRepository = rateCardRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.pricingTimer = meterRegistry.timer("billing.pricing.calculation.time");
        this.successCounter = meterRegistry.counter("billing.pricing.success");
        this.errorCounter = meterRegistry.counter("billing.pricing.error");
    }

    /**
     * Calculate price (simple BigDecimal return for backward compatibility).
     */
    public BigDecimal calculatePrice(String clientId, OrderContextDto order) {
        return calculatePriceWithBreakdown(clientId, order).getTotalAmount();
    }

    /**
     * Calculate price with detailed breakdown.
     */
    public CalculationBreakdownDto calculatePriceWithBreakdown(String clientId, OrderContextDto order) {
        return pricingTimer.record(() -> {
            try {
                CalculationBreakdownDto result = performCalculation(clientId, order);
                successCounter.increment();
                return result;
            } catch (Exception e) {
                errorCounter.increment();
                throw e;
            }
        });
    }

    private CalculationBreakdownDto performCalculation(String clientId, OrderContextDto order) {
        // Validation
        validateOrderContext(order);
        
        // 1. Find Active Rate Card
        List<RateCardEntity> rateCards = rateCardRepository.findByClientIdAndActiveTrue(clientId);
        if (rateCards.isEmpty()) {
            log.error("No active RateCard found for Client {}", clientId);
            throw new RateCardNotFoundException(clientId);
        }
        RateCardEntity rateCard = rateCards.get(0);

        // 2. Fetch Rules
        List<PricingRuleEntity> rules = pricingRuleRepository.findByRateCard(rateCard);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<CalculationBreakdownDto.AppliedRuleDto> appliedRules = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 3. Evaluate Rules
        for (PricingRuleEntity rule : rules) {
            BigDecimal metricValue = getMetricValue(order, rule.getMetric());
            
            // Check if metric has value
            if (metricValue.compareTo(BigDecimal.ZERO) == 0 && rule.getMetric() != PricingRuleEntity.MetricType.ITEM) {
                warnings.add(String.format("No %s value provided, %s charge skipped", 
                        rule.getMetric(), rule.getMetric()));
                continue;
            }
            
            // Check Range
            boolean inRange = metricValue.compareTo(rule.getRangeStart()) >= 0
                    && (rule.getRangeEnd() == null || metricValue.compareTo(rule.getRangeEnd()) < 0);

            if (inRange) {
                // Calculate Rule Cost
                BigDecimal ruleCost = rule.getBasePrice();
                String calculation;
                
                if (rule.getUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal variableCost = rule.getUnitPrice().multiply(metricValue);
                    ruleCost = ruleCost.add(variableCost);
                    calculation = String.format("€%.2f + (%s %s × €%.2f/%s) = €%.2f", 
                            rule.getBasePrice(),
                            metricValue,
                            getUnitForMetric(rule.getMetric()),
                            rule.getUnitPrice(),
                            getUnitForMetric(rule.getMetric()),
                            ruleCost);
                } else {
                    calculation = String.format("€%.2f (flat rate)", ruleCost);
                }

                log.info("Rule Applied: {} | {} = {}", 
                        rule.getMetric(), calculation, ruleCost);
                
                appliedRules.add(CalculationBreakdownDto.AppliedRuleDto.builder()
                        .ruleName(formatRuleName(rule))
                        .ruleType(rule.getMetric().toString())
                        .metricValue(metricValue)
                        .unit(getUnitForMetric(rule.getMetric()))
                        .basePrice(rule.getBasePrice())
                        .unitPrice(rule.getUnitPrice())
                        .subtotal(ruleCost)
                        .calculation(calculation)
                        .build());
                
                totalPrice = totalPrice.add(ruleCost);
            }
        }

        if (appliedRules.isEmpty()) {
            warnings.add("No pricing rules applied. Order may be missing required metrics.");
        }

        return CalculationBreakdownDto.builder()
                .totalAmount(totalPrice)
                .currency(rateCard.getCurrency())
                .appliedRules(appliedRules)
                .warnings(warnings.isEmpty() ? null : warnings)
                .build();
    }

    /**
     * Validate order context.
     */
    private void validateOrderContext(OrderContextDto order) {
        if (order == null) {
            throw InvalidOrderContextException.missingRequired("order");
        }
        
        // Check for negative values
        if (order.getWeight() != null && order.getWeight().compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidOrderContextException.negativeValue("weight", order.getWeight());
        }
        if (order.getDistance() != null && order.getDistance().compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidOrderContextException.negativeValue("distance", order.getDistance());
        }
        if (order.getVolume() != null && order.getVolume().compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidOrderContextException.negativeValue("volume", order.getVolume());
        }
        if (order.getItemCount() != null && order.getItemCount() < 0) {
            throw InvalidOrderContextException.negativeValue("itemCount", order.getItemCount());
        }
    }

    private BigDecimal getMetricValue(OrderContextDto order, PricingRuleEntity.MetricType metric) {
        switch (metric) {
            case WEIGHT: return order.getWeight() != null ? order.getWeight() : BigDecimal.ZERO;
            case VOLUME: return order.getVolume() != null ? order.getVolume() : BigDecimal.ZERO;
            case COUNT: return order.getItemCount() != null ? BigDecimal.valueOf(order.getItemCount()) : BigDecimal.ZERO;
            case DISTANCE: return order.getDistance() != null ? order.getDistance() : BigDecimal.ZERO;
            case ITEM: return BigDecimal.ONE;
            default: return BigDecimal.ZERO;
        }
    }

    private String getUnitForMetric(PricingRuleEntity.MetricType metric) {
        switch (metric) {
            case WEIGHT: return "kg";
            case VOLUME: return "m³";
            case COUNT: return "items";
            case DISTANCE: return "km";
            case ITEM: return "order";
            default: return "";
        }
    }

    private String formatRuleName(PricingRuleEntity rule) {
        String range = rule.getRangeEnd() != null 
                ? String.format("%.0f-%.0f%s", rule.getRangeStart(), rule.getRangeEnd(), getUnitForMetric(rule.getMetric()))
                : String.format("%.0f%s+", rule.getRangeStart(), getUnitForMetric(rule.getMetric()));
        
        return String.format("%s (%s)", rule.getMetric(), range);
    }
}
