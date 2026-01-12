package com.example.planning_service.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics configuration for planning service.
 * Tracks zone resolution, optimization, and route operations.
 */
@Configuration
public class MetricsConfig {

    /**
     * Timer for zone resolution operations.
     */
    @Bean
    public Timer zoneResolutionTimer(MeterRegistry registry) {
        return Timer.builder("planning.zone.resolution.time")
                .description("Time taken to resolve zone from postal code")
                .tag("service", "planning")
                .register(registry);
    }

    /**
     * Timer for optimization calculations.
     */
    @Bean
    public Timer optimizationTimer(MeterRegistry registry) {
        return Timer.builder("planning.optimization.time")
                .description("Time taken to optimize routes")
                .tag("service", "planning")
                .register(registry);
    }

    /**
     * Counter for successful zone resolutions.
     */
    @Bean
    public Counter zoneResolutionSuccessCounter(MeterRegistry registry) {
        return Counter.builder("planning.zone.resolution.success")
                .description("Successful zone resolutions")
                .tag("service", "planning")
                .register(registry);
    }

    /**
     * Counter for cache hits.
     */
    @Bean
    public Counter zoneCacheHitCounter(MeterRegistry registry) {
        return Counter.builder("planning.zone.resolution.cache.hit")
                .description("Zone resolution cache hits")
                .tag("service", "planning")
                .register(registry);
    }

    /**
     * Counter for cache misses.
     */
    @Bean
    public Counter zoneCacheMissCounter(MeterRegistry registry) {
        return Counter.builder("planning.zone.resolution.cache.miss")
                .description("Zone resolution cache misses")
                .tag("service", "planning")
                .register(registry);
    }

    /**
     * Counter for routes created.
     */
    @Bean
    public Counter routesCreatedCounter(MeterRegistry registry) {
        return Counter.builder("planning.routes.created")
                .description("Total routes created")
                .tag("service", "planning")
                .register(registry);
    }

    /**
     * Counter for routes completed.
     */
    @Bean
    public Counter routesCompletedCounter(MeterRegistry registry) {
        return Counter.builder("planning.routes.completed")
                .description("Total routes completed")
                .tag("service", "planning")
                .register(registry);
    }
}
