package com.example.planning_service.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * ARCHITEKTURA: Caching Configuration dla Google Maps API
 * 
 * Cache distance matrix results dla 30 minut:
 * - Traffic patterns change, ale nie co minutę
 * - Nocne dostawy (23:00-08:00) mają stabilniejszy traffic
 * - Cache reduces API costs (€200/mies limit)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Simple in-memory cache dla distance matrix
     * 
     * For production: Consider Redis cache jeśli masz multiple instances
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "distanceMatrix", // Google Maps distance cache
                "geocodingResults", // Address geocoding cache
                "hubAssignments" // Hub-to-location mappings
        );
    }
}
