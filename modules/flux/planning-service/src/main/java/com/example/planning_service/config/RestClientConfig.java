package com.example.planning_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for HTTP clients used by external service integrations
 * (Google Maps Distance Matrix API, etc.)
 */
@Configuration
public class RestClientConfig {

    /**
     * Create a RestClient bean for making HTTP requests to external APIs.
     * Used by GoogleMapsDistanceService for distance matrix calculations.
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .defaultHeader("User-Agent", "VoidTracker-Planning-Service/1.0")
                .build();
    }
}
