package com.example.planning_service.service;

import com.example.planning_service.entity.PostalCodeRuleEntity;
import com.example.planning_service.entity.ZoneDefinitionEntity;
import com.example.planning_service.repository.PostalCodeRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ZoneResolutionService {

    private final PostalCodeRuleRepository ruleRepository;

    /**
     * Resolves a Zone for a given postal code.
     * Uses string comparison for ranges (lexicographical check works for standard formats like PL "00-000").
     */
    @Cacheable("zone-resolution")
    public Optional<ZoneDefinitionEntity> resolveZone(String countryCode, String postalCode) {
        if (countryCode == null || postalCode == null) return Optional.empty();

        // Fetch all rules for the country ordered by priority
        // In a real high-throughput system, this list should be cached per country.
        List<PostalCodeRuleEntity> rules = ruleRepository.findAllByCountryCode(countryCode);

        for (PostalCodeRuleEntity rule : rules) {
            String start = rule.getPostalCodeStart();
            String end = rule.getPostalCodeEnd();

            // Check range: start <= postalCode <= end
            // String comparison is valid for uniform length codes (e.g. PL 00-000 vs 99-999)
            if (postalCode.compareTo(start) >= 0 && postalCode.compareTo(end) <= 0) {
                log.debug("Resolved PostalCode {} to Zone {}", postalCode, rule.getZone().getCode());
                return Optional.of(rule.getZone());
            }
        }

        log.debug("No Zone found for PostalCode {} in {}", postalCode, countryCode);
        return Optional.empty();
    }
}
