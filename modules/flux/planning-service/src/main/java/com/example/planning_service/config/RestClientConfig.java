package com.example.planning_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for HTTP clients used by external service integrations
 * (Google Maps Distance Matrix API, n8n webhooks, etc.)
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

    /**
     * Create a RestTemplate bean for n8n webhook integration
     * Used by GatekeeperService for AI Agent communication
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
