package com.example.planning_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ARCHITEKTURA: Google Maps Platform Configuration
 * 
 * Konfiguracja dla Google Maps API integracji:
 * - Distance Matrix API (traffic-aware distances)
 * - Directions API (optimal routes)
 * - Geocoding API (address validation)
 * 
 * Cost Estimate: ~€200/mies (~880 PLN/mies) dla nocnych dostaw
 * 
 * Expected Benefits:
 * - 5-10% fuel savings (traffic-aware routing)
 * - 15% better ETA accuracy
 * - Real-time congestion awareness
 */
@Configuration
@ConfigurationProperties(prefix = "google.maps")
@Data
public class GoogleMapsProperties {

    /**
     * Google Maps API key
     * Get from: https://console.cloud.google.com/apis/credentials
     */
    private String apiKey;

    /**
     * Enable/disable Google Maps integration
     */
    private boolean enabled = false;

    /**
     * Traffic model for nocne dostawy (best_guess, optimistic, pessimistic)
     */
    private TrafficModel trafficModel = TrafficModel.BEST_GUESS;

    /**
     * Cache duration for distance matrix (in minutes)
     * Traffic changes, so don't cache too long for overnight deliveries
     */
    private int cacheExpirationMinutes = 30;

    /**
     * Max locations per distance matrix request (Google limit: 100)
     */
    private int maxLocationsPerRequest = 100;

    /**
     * Request timeout in seconds
     */
    private int requestTimeoutSeconds = 10;

    /**
     * Fallback to straight-line distance if Google Maps fails
     */
    private boolean fallbackToStraightLine = true;

    /**
     * Average speed for fallback calculations (km/h)
     * Nocne dostawy: 70 km/h average (highways bez traffic)
     */
    private double fallbackAverageSpeedKmh = 70.0;

    public enum TrafficModel {
        BEST_GUESS, // Balanced prediction (recommended dla nocnych dostaw)
        OPTIMISTIC, // Assumes minimal traffic (use for testing)
        PESSIMISTIC // Assumes heavy traffic (conservative estimate)
    }

    /**
     * Validate configuration on startup
     */
    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }
}
