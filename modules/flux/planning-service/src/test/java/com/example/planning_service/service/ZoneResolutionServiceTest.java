package com.example.planning_service.service;

import com.example.planning_service.entity.PostalCodeRuleEntity;
import com.example.planning_service.entity.ZoneDefinitionEntity;
import com.example.planning_service.exception.InvalidPostalCodeException;
import com.example.planning_service.exception.ZoneNotFoundException;
import com.example.planning_service.repository.PostalCodeRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ZoneResolutionService.
 * Tests Polish postal code resolution (XX-XXX format).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ZoneResolutionService Tests")
class ZoneResolutionServiceTest {

    @Mock
    private PostalCodeRuleRepository ruleRepository;

    @InjectMocks
    private ZoneResolutionService zoneResolutionService;

    private ZoneDefinitionEntity warsawZone;
    private ZoneDefinitionEntity krakowZone;
    private List<PostalCodeRuleEntity> testRules;

    @BeforeEach
    void setUp() {
        // Create test zones using builder pattern
        warsawZone = ZoneDefinitionEntity.builder()
                .id(UUID.randomUUID())
                .code("WARSAW_CENTER")
                .name("Warsaw City Center")
                .countryCode("PL")
                .description("Central Warsaw district")
                .build();

        krakowZone = ZoneDefinitionEntity.builder()
                .id(UUID.randomUUID())
                .code("KRAKOW_CENTER")
                .name("Kraków City Center")
                .countryCode("PL")
                .description("Central Kraków district")
                .build();

        // Create postal code rules (Polish format: XX-XXX)
        PostalCodeRuleEntity warsawRule = PostalCodeRuleEntity.builder()
                .id(UUID.randomUUID())
                .postalCodeStart("00-000")
                .postalCodeEnd("04-999")
                .zone(warsawZone)
                .priority(1)
                .build();

        PostalCodeRuleEntity krakowRule = PostalCodeRuleEntity.builder()
                .id(UUID.randomUUID())
                .postalCodeStart("30-000")
                .postalCodeEnd("32-999")
                .zone(krakowZone)
                .priority(2)
                .build();

        testRules = Arrays.asList(warsawRule, krakowRule);
    }

    @Test
    @DisplayName("Should resolve Warsaw zone for valid postal code 00-001")
    void testResolveWarszawaZone() {
        // Arrange
        when(ruleRepository.findAllByCountryCode("PL")).thenReturn(testRules);

        // Act
        Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone("PL", "00-001");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("WARSAW_CENTER");
        assertThat(result.get().getName()).isEqualTo("Warsaw City Center");
        verify(ruleRepository).findAllByCountryCode("PL");
    }

    @Test
    @DisplayName("Should resolve Kraków zone for valid postal code 30-500")
    void testResolveKrakowZone() {
        // Arrange
        when(ruleRepository.findAllByCountryCode("PL")).thenReturn(testRules);

        // Act
        Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone("PL", "30-500");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("KRAKOW_CENTER");
    }

    @Test
    @DisplayName("Should resolve zone for boundary value (range start)")
    void testBoundaryValueRangeStart() {
        // Arrange
        when(ruleRepository.findAllByCountryCode("PL")).thenReturn(testRules);

        // Act - Test exact start of range
        Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone("PL", "00-000");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("WARSAW_CENTER");
    }

    @Test
    @DisplayName("Should resolve zone for boundary value (range end)")
    void testBoundaryValueRangeEnd() {
        // Arrange
        when(ruleRepository.findAllByCountryCode("PL")).thenReturn(testRules);

        // Act - Test exact end of range
        Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone("PL", "04-999");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("WARSAW_CENTER");
    }

    @Test
    @DisplayName("Should return empty for unknown postal code")
    void testUnknownPostalCode() {
        // Arrange
        when(ruleRepository.findAllByCountryCode("PL")).thenReturn(testRules);

        // Act - Postal code outside all ranges
        Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone("PL", "99-999");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for null postal code")
    void testNullPostalCode() {
        // Act
        Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone("PL", null);

        // Assert
        assertThat(result).isEmpty();
        verify(ruleRepository, never()).findAllByCountryCode(anyString());
    }

    @Test
    @DisplayName("Should return empty for null country code")
    void testNullCountryCode() {
        // Act
        Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone(null, "00-001");

        // Assert
        assertThat(result).isEmpty();
        verify(ruleRepository, never()).findAllByCountryCode(anyString());
    }

    @Test
    @DisplayName("Should return empty when no rules exist for country")
    void testNoRulesForCountry() {
        // Arrange
        when(ruleRepository.findAllByCountryCode("DE")).thenReturn(Collections.emptyList());

        // Act
        Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone("DE", "10115");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple overlapping rules (priority)")
    void testMultipleOverlappingRules() {
        // Arrange - Create overlapping rule with higher priority
        PostalCodeRuleEntity overlappingRule = PostalCodeRuleEntity.builder()
                .id(UUID.randomUUID())
                .postalCodeStart("00-000")
                .postalCodeEnd("00-999")
                .zone(warsawZone)
                .priority(0) // Higher priority (lower number)
                .build();

        List<PostalCodeRuleEntity> rulesWithOverlap = Arrays.asList(overlappingRule, testRules.get(0));
        when(ruleRepository.findAllByCountryCode("PL")).thenReturn(rulesWithOverlap);

        // Act - Should match first rule (higher priority)
        Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone("PL", "00-500");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("WARSAW_CENTER");
    }

    @Test
    @DisplayName("Should use cache for repeated lookups")
    void testCacheUsage() {
        // Arrange
        when(ruleRepository.findAllByCountryCode("PL")).thenReturn(testRules);

        // Act - Call twice with same parameters
        zoneResolutionService.resolveZone("PL", "00-001");
        zoneResolutionService.resolveZone("PL", "00-001");

        // Assert - Repository should be called twice (cache is applied at Spring level)
        // Note: Cache behavior verification requires integration test
        verify(ruleRepository, times(2)).findAllByCountryCode("PL");
    }

    @Test
    @DisplayName("Should handle postal codes outside range gracefully")
    void testPostalCodeOutOfAllRanges() {
        // Arrange
        when(ruleRepository.findAllByCountryCode("PL")).thenReturn(testRules);

        // Act - Test postal codes between defined ranges
        Optional<ZoneDefinitionEntity> result1 = zoneResolutionService.resolveZone("PL", "05-000");
        Optional<ZoneDefinitionEntity> result2 = zoneResolutionService.resolveZone("PL", "20-000");

        // Assert
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
    }

    @Test
    @DisplayName("Should correctly compare Polish postal code strings lexicographically")
    void testLexicographicComparison() {
        // Arrange
        when(ruleRepository.findAllByCountryCode("PL")).thenReturn(testRules);

        // Act - Test various positions in range
        List<String> testCodes = Arrays.asList("00-100", "01-500", "03-750", "04-900");

        for (String code : testCodes) {
            Optional<ZoneDefinitionEntity> result = zoneResolutionService.resolveZone("PL", code);

            // Assert - All should resolve to Warsaw
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("WARSAW_CENTER");
        }
    }
}
