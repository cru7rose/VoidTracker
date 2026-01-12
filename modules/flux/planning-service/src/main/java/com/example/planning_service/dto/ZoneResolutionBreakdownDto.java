package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed breakdown of zone resolution process.
 * Shows exactly how a postal code was matched to a zone.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneResolutionBreakdownDto {

    private String postalCode;
    private String countryCode;
    private ZoneInfo resolvedZone;
    private MatchedRuleInfo matchedRule;
    private Boolean cached;
    private Long resolutionTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneInfo {
        private Long id;
        private String code;
        private String name;
        private String description;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchedRuleInfo {
        private Long id;
        private String rangeStart;
        private String rangeEnd;
        private Integer priority;
        private String ruleName;
    }

    /**
     * Format for Polish postal codes (PL): XX-XXX (e.g., 00-001, 99-999)
     */
    public static String getPolishPostalCodeFormat() {
        return "XX-XXX (e.g., 00-001)";
    }
}
