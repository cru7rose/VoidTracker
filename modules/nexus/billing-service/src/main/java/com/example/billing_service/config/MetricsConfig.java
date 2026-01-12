package com.example.billing_service.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ARCHITEKTURA: Metrics configuration for billing service.
 * Configures Micrometer for monitoring pricing calculations, invoice generation, and errors.
 */
@Configuration
public class MetricsConfig {

    /**
     * Timer for pricing calculations.
     */
    @Bean
    public Timer pricingCalculationTimer(MeterRegistry registry) {
        return Timer.builder("billing.pricing.calculation.time")
                .description("Time taken to calculate pricing")
                .tag("service", "billing")
                .register(registry);
    }

    /**
     * Timer for invoice generation.
     */
    @Bean
    public Timer invoiceGenerationTimer(MeterRegistry registry) {
        return Timer.builder("billing.invoice.generation.time")
                .description("Time taken to generate invoice")
                .tag("service", "billing")
                .register(registry);
    }
}
