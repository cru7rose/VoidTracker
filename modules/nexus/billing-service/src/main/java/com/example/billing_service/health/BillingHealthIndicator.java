package com.example.billing_service.health;

import com.example.billing_service.repository.RateCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * ARCHITEKTURA: Health indicator for billing service.
 * Checks if rate cards are available for billing operations.
 */
@Component
@RequiredArgsConstructor
public class BillingHealthIndicator implements HealthIndicator {

    private final RateCardRepository rateCardRepository;

    @Override
    public Health health() {
        try {
            // Check if we have any active rate cards
            long activeRateCards = rateCardRepository.count();
            
            if (activeRateCards == 0) {
                return Health.down()
                        .withDetail("reason", "No rate cards configured")
                        .withDetail("activeRateCards", 0)
                        .build();
            }
            
            return Health.up()
                    .withDetail("activeRateCards", activeRateCards)
                    .withDetail("status", "Billing system operational")
                    .build();
                    
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
