package com.example.planning_service.health;

import com.example.planning_service.repository.PostalCodeRuleRepository;
import com.example.planning_service.repository.ZoneDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for planning service.
 * Checks if zone definitions and postal code rules are available.
 */
@Component
@RequiredArgsConstructor
public class PlanningHealthIndicator implements HealthIndicator {

    private final ZoneDefinitionRepository zoneRepository;
    private final PostalCodeRuleRepository postalCodeRuleRepository;

    @Override
    public Health health() {
        try {
            long zoneCount = zoneRepository.count();
            long ruleCount = postalCodeRuleRepository.count();

            if (zoneCount == 0) {
                return Health.up()
                        .withDetail("reason", "No zone definitions configured")
                        .withDetail("zoneCount", 0)
                        .withDetail("status", "System ready for configuration")
                        .build();
            }

            if (ruleCount == 0) {
                return Health.up()
                        .withDetail("reason", "No postal code rules configured")
                        .withDetail("ruleCount", 0)
                        .withDetail("status", "System ready for configuration")
                        .build();
            }

            return Health.up()
                    .withDetail("zoneCount", zoneCount)
                    .withDetail("postalCodeRules", ruleCount)
                    .withDetail("status", "Planning service operational")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
