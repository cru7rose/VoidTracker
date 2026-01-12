package com.example.billing_service.service;

import com.example.billing_service.dto.CalculationBreakdownDto;
import com.example.billing_service.dto.OrderContextDto;
import com.example.billing_service.entity.PricingRuleEntity;
import com.example.billing_service.entity.RateCardEntity;
import com.example.billing_service.exception.InvalidOrderContextException;
import com.example.billing_service.exception.RateCardNotFoundException;
import com.example.billing_service.repository.PricingRuleRepository;
import com.example.billing_service.repository.RateCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for PricingEngine.
 * Tests various scenarios: standard orders, edge cases, exceptions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PricingEngine Tests")
class PricingEngineTest {

    @Mock
    private RateCardRepository rateCardRepository;

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    @InjectMocks
    private PricingEngine pricingEngine;

    private RateCardEntity testRateCard;
    private List<PricingRuleEntity> testRules;

    @BeforeEach
    void setUp() {
        // Create test rate card
        testRateCard = RateCardEntity.builder()
                .name("Test Rate Card")
                .clientId("TEST_CLIENT")
                .currency("EUR")
                .validFrom(LocalDate.now())
                .active(true)
                .build();

        // Create test pricing rules matching the seeded data
        PricingRuleEntity weightRule1 = PricingRuleEntity.builder()
                .rateCard(testRateCard)
                .metric(PricingRuleEntity.MetricType.WEIGHT)
                .rangeStart(new BigDecimal("0"))
                .rangeEnd(new BigDecimal("10"))
                .basePrice(new BigDecimal("15.00"))
                .unitPrice(new BigDecimal("1.50"))
                .build();

        PricingRuleEntity weightRule2 = PricingRuleEntity.builder()
                .rateCard(testRateCard)
                .metric(PricingRuleEntity.MetricType.WEIGHT)
                .rangeStart(new BigDecimal("10"))
                .rangeEnd(new BigDecimal("50"))
                .basePrice(new BigDecimal("30.00"))
                .unitPrice(new BigDecimal("2.00"))
                .build();

        PricingRuleEntity distanceRule1 = PricingRuleEntity.builder()
                .rateCard(testRateCard)
                .metric(PricingRuleEntity.MetricType.DISTANCE)
                .rangeStart(new BigDecimal("0"))
                .rangeEnd(new BigDecimal("10"))
                .basePrice(new BigDecimal("5.00"))
                .unitPrice(BigDecimal.ZERO)
                .build();

        PricingRuleEntity distanceRule2 = PricingRuleEntity.builder()
                .rateCard(testRateCard)
                .metric(PricingRuleEntity.MetricType.DISTANCE)
                .rangeStart(new BigDecimal("10"))
                .rangeEnd(new BigDecimal("50"))
                .basePrice(new BigDecimal("10.00"))
                .unitPrice(new BigDecimal("0.50"))
                .build();

        PricingRuleEntity itemRule = PricingRuleEntity.builder()
                .rateCard(testRateCard)
                .metric(PricingRuleEntity.MetricType.ITEM)
                .rangeStart(BigDecimal.ZERO)
                .rangeEnd(null)
                .basePrice(new BigDecimal("3.00"))
                .unitPrice(BigDecimal.ZERO)
                .build();

        testRules = Arrays.asList(weightRule1, weightRule2, distanceRule1, distanceRule2, itemRule);
    }

    @Test
    @DisplayName("Should calculate correct price for standard order (10.5kg @ 15.2km)")
    void testStandardOrder() {
        // Arrange
        when(rateCardRepository.findByClientIdAndActiveTrue("TEST_CLIENT"))
                .thenReturn(Collections.singletonList(testRateCard));
        when(pricingRuleRepository.findByRateCard(testRateCard)).thenReturn(testRules);

        OrderContextDto order = OrderContextDto.builder()
                .weight(new BigDecimal("10.5"))
                .distance(new BigDecimal("15.2"))
                .build();

        // Act
        BigDecimal result = pricingEngine.calculatePrice("TEST_CLIENT", order);

        // Assert
        // Weight (10-50kg): €30 + (10.5 × €2) = €51.00
        // Distance (10-50km): €10 + (15.2 × €0.50) = €17.60
        // Item: €3.00
        // Total: €71.60
        assertThat(result).isEqualByComparingTo(new BigDecimal("71.60"));
    }

    @Test
    @DisplayName("Should provide detailed breakdown with applied rules")
    void testCalculateWithBreakdown() {
        // Arrange
        when(rateCardRepository.findByClientIdAndActiveTrue("TEST_CLIENT"))
                .thenReturn(Collections.singletonList(testRateCard));
        when(pricingRuleRepository.findByRateCard(testRateCard)).thenReturn(testRules);

        OrderContextDto order = OrderContextDto.builder()
                .weight(new BigDecimal("10.5"))
                .distance(new BigDecimal("15.2"))
                .build();

        // Act
        CalculationBreakdownDto breakdown = pricingEngine.calculatePriceWithBreakdown("TEST_CLIENT", order);

        // Assert
        assertThat(breakdown.getTotalAmount()).isEqualByComparingTo(new BigDecimal("71.60"));
        assertThat(breakdown.getCurrency()).isEqualTo("EUR");
        assertThat(breakdown.getAppliedRules()).hasSize(3);
        assertThat(breakdown.getAppliedRules())
                .extracting(CalculationBreakdownDto.AppliedRuleDto::getRuleType)
                .containsExactlyInAnyOrder("WEIGHT", "DISTANCE", "ITEM");
    }

    @Test
    @DisplayName("Should calculate correctly for light package (5kg @ 8km)")
    void testLightPackage() {
        // Arrange
        when(rateCardRepository.findByClientIdAndActiveTrue("TEST_CLIENT"))
                .thenReturn(Collections.singletonList(testRateCard));
        when(pricingRuleRepository.findByRateCard(testRateCard)).thenReturn(testRules);

        OrderContextDto order = OrderContextDto.builder()
                .weight(new BigDecimal("5"))
                .distance(new BigDecimal("8"))
                .build();

        // Act
        BigDecimal result = pricingEngine.calculatePrice("TEST_CLIENT", order);

        // Assert
        // Weight (0-10kg): €15 + (5 × €1.50) = €22.50
        // Distance (0-10km): €5.00 (flat)
        // Item: €3.00
        // Total: €30.50
        assertThat(result).isEqualByComparingTo(new BigDecimal("30.50"));
    }

    @Test
    @DisplayName("Should calculate correctly for heavy package (75kg @ 5km)")
    void testHeavyPackage() {
        // Create heavy tier rule
        PricingRuleEntity heavyRule = PricingRuleEntity.builder()
                .rateCard(testRateCard)
                .metric(PricingRuleEntity.MetricType.WEIGHT)
                .rangeStart(new BigDecimal("50"))
                .rangeEnd(null)
                .basePrice(new BigDecimal("60.00"))
                .unitPrice(new BigDecimal("2.50"))
                .build();

        List<PricingRuleEntity> rulesWithHeavy = Arrays.asList(
                testRules.get(0), testRules.get(1), heavyRule,
                testRules.get(2), testRules.get(3), testRules.get(4)
        );

        when(rateCardRepository.findByClientIdAndActiveTrue("TEST_CLIENT"))
                .thenReturn(Collections.singletonList(testRateCard));
        when(pricingRuleRepository.findByRateCard(testRateCard)).thenReturn(rulesWithHeavy);

        OrderContextDto order = OrderContextDto.builder()
                .weight(new BigDecimal("75"))
                .distance(new BigDecimal("5"))
                .build();

        // Act
        BigDecimal result = pricingEngine.calculatePrice("TEST_CLIENT", order);

        // Assert
        // Weight (50kg+): €60 + (75 × €2.50) = €247.50
        // Distance (0-10km): €5.00
        // Item: €3.00
        // Total: €255.50
        assertThat(result).isEqualByComparingTo(new BigDecimal("255.50"));
    }

    @Test
    @DisplayName("Should throw RateCardNotFoundException when no rate card exists")
    void testNoRateCard() {
        // Arrange
        when(rateCardRepository.findByClientIdAndActiveTrue("INVALID_CLIENT"))
                .thenReturn(Collections.emptyList());

        OrderContextDto order = OrderContextDto.builder()
                .weight(new BigDecimal("10"))
                .build();

        // Act & Assert
        assertThatThrownBy(() -> pricingEngine.calculatePrice("INVALID_CLIENT", order))
                .isInstanceOf(RateCardNotFoundException.class)
                .hasMessageContaining("INVALID_CLIENT");
    }

    @Test
    @DisplayName("Should throw InvalidOrderContextException for negative weight")
    void testNegativeWeight() {
        // Arrange
        OrderContextDto order = OrderContextDto.builder()
                .weight(new BigDecimal("-5"))
                .build();

        // Act & Assert - validation happens before repository calls
        assertThatThrownBy(() -> pricingEngine.calculatePrice("TEST_CLIENT", order))
                .isInstanceOf(InvalidOrderContextException.class)
                .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("Should throw InvalidOrderContextException for null order")
    void testNullOrder() {
        // Act & Assert
        assertThatThrownBy(() -> pricingEngine.calculatePrice("TEST_CLIENT", null))
                .isInstanceOf(InvalidOrderContextException.class)
                .hasMessageContaining("order");
    }

    @Test
    @DisplayName("Should handle order with missing metrics gracefully")
    void testMissingMetrics() {
        // Arrange
        when(rateCardRepository.findByClientIdAndActiveTrue("TEST_CLIENT"))
                .thenReturn(Collections.singletonList(testRateCard));
        when(pricingRuleRepository.findByRateCard(testRateCard)).thenReturn(testRules);

        OrderContextDto order = OrderContextDto.builder()
                .weight(new BigDecimal("10.5"))
                // No distance
                .build();

        // Act
        CalculationBreakdownDto breakdown = pricingEngine.calculatePriceWithBreakdown("TEST_CLIENT", order);

        // Assert
        // Only weight and item should apply
        assertThat(breakdown.getAppliedRules()).hasSize(2);
        assertThat(breakdown.getAppliedRules())
                .extracting(CalculationBreakdownDto.AppliedRuleDto::getRuleType)
                .containsExactlyInAnyOrder("WEIGHT", "ITEM");
        
        // Should have warning about missing distance
        assertThat(breakdown.getWarnings()).isNotNull();
        assertThat(breakdown.getWarnings()).anyMatch(w -> w.contains("DISTANCE"));
    }

    @Test
    @DisplayName("Should calculate item fee even with zero weight/distance")
    void testItemFeeOnly() {
        // Arrange
        when(rateCardRepository.findByClientIdAndActiveTrue("TEST_CLIENT"))
                .thenReturn(Collections.singletonList(testRateCard));
        when(pricingRuleRepository.findByRateCard(testRateCard)).thenReturn(testRules);

        OrderContextDto order = OrderContextDto.builder()
                .weight(BigDecimal.ZERO)
                .distance(BigDecimal.ZERO)
                .build();

        // Act
        BigDecimal result = pricingEngine.calculatePrice("TEST_CLIENT", order);

        // Assert - should only charge item fee
        assertThat(result).isEqualByComparingTo(new BigDecimal("3.00"));
    }

    @Test
    @DisplayName("Should include calculation details in breakdown")
    void testCalculationDetails() {
        // Arrange
        when(rateCardRepository.findByClientIdAndActiveTrue("TEST_CLIENT"))
                .thenReturn(Collections.singletonList(testRateCard));
        when(pricingRuleRepository.findByRateCard(testRateCard)).thenReturn(testRules);

        OrderContextDto order = OrderContextDto.builder()
                .weight(new BigDecimal("10.5"))
                .distance(new BigDecimal("15.2"))
                .build();

        // Act
        CalculationBreakdownDto breakdown = pricingEngine.calculatePriceWithBreakdown("TEST_CLIENT", order);

        // Assert - verify calculation strings are present
        CalculationBreakdownDto.AppliedRuleDto weightRule = breakdown.getAppliedRules().stream()
                .filter(r -> r.getRuleType().equals("WEIGHT"))
                .findFirst()
                .orElseThrow();

        assertThat(weightRule.getCalculation()).contains("€30.00");
        assertThat(weightRule.getCalculation()).contains("10.5");
        assertThat(weightRule.getCalculation()).contains("€2.00");
        assertThat(weightRule.getCalculation()).contains("€51.00");
    }

    @Test
    @DisplayName("Should verify rate card is active before calculation")
    void testRateCardActivation() {
        // Arrange
        when(rateCardRepository.findByClientIdAndActiveTrue("TEST_CLIENT"))
                .thenReturn(Collections.singletonList(testRateCard));
        when(pricingRuleRepository.findByRateCard(testRateCard)).thenReturn(testRules);

        OrderContextDto order = OrderContextDto.builder()
                .weight(new BigDecimal("5"))
                .build();

        // Act
        pricingEngine.calculatePrice("TEST_CLIENT", order);

        // Assert - verify correct repository method was called
        verify(rateCardRepository).findByClientIdAndActiveTrue("TEST_CLIENT");
    }
}
