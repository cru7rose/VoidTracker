package com.example.planning_service.service;

import com.example.planning_service.config.AutoPlanProperties;
import com.example.planning_service.domain.HubConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Hub Management Service
 * 
 * Zarządza konfiguracją hubów (centrów dystrybucyjnych).
 * W aktualnej implementacji czyta z YAML config (auto-plan.hubs).
 * 
 * Future Enhancement: Może być rozszerzone o database-backed CRUD
 * dla dynamic hub management przez dispatcher UI.
 * 
 * User Requirement: "konfigurowalne, elastycznie z hubów"
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HubService {

    private final AutoPlanProperties autoPlanProperties;

    /**
     * Get all configured hubs from YAML
     */
    public List<HubConfiguration> getAllHubs() {
        return autoPlanProperties.getHubs().stream()
                .map(this::convertToHubConfiguration)
                .collect(Collectors.toList());
    }

    /**
     * Get only active/operational hubs
     */
    public List<HubConfiguration> getActiveHubs() {
        return getAllHubs().stream()
                .filter(HubConfiguration::isOperational)
                .collect(Collectors.toList());
    }

    /**
     * Find hub by ID
     */
    public Optional<HubConfiguration> getHubById(String hubId) {
        return getAllHubs().stream()
                .filter(hub -> hub.getId().equals(hubId))
                .findFirst();
    }

    /**
     * Find nearest hub to a given location
     * 
     * @param latitude  Order delivery latitude
     * @param longitude Order delivery longitude
     * @return Nearest operational hub, or empty if none found
     */
    public Optional<HubConfiguration> findNearestHub(double latitude, double longitude) {
        HubConfiguration.Location orderLocation = HubConfiguration.Location.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();

        return getActiveHubs().stream()
                .min((h1, h2) -> {
                    double dist1 = h1.getLocation().distanceToKm(orderLocation);
                    double dist2 = h2.getLocation().distanceToKm(orderLocation);
                    return Double.compare(dist1, dist2);
                });
    }

    /**
     * Find hub that covers a specific location (within catchment area)
     * 
     * @param latitude  Order delivery latitude
     * @param longitude Order delivery longitude
     * @return Hub covering this location, or nearest hub if none in catchment
     */
    public Optional<HubConfiguration> findHubForLocation(double latitude, double longitude) {
        HubConfiguration.Location orderLocation = HubConfiguration.Location.builder()
                .latitude(latitude)
                .longitude(longitude)
                .build();

        // First, try to find hub where location is within catchment area
        Optional<HubConfiguration> hubInCatchment = getActiveHubs().stream()
                .filter(hub -> hub.isWithinCatchmentArea(orderLocation))
                .findFirst();

        if (hubInCatchment.isPresent()) {
            log.debug("Order at ({}, {}) assigned to hub {} (within catchment)",
                    latitude, longitude, hubInCatchment.get().getId());
            return hubInCatchment;
        }

        // If no hub covers this area, assign to nearest hub
        Optional<HubConfiguration> nearestHub = findNearestHub(latitude, longitude);
        if (nearestHub.isPresent()) {
            log.warn("Order at ({}, {}) outside all catchment areas, assigned to nearest hub {}",
                    latitude, longitude, nearestHub.get().getId());
        }
        return nearestHub;
    }

    /**
     * Get hub statistics for monitoring
     */
    public HubStatistics getHubStatistics() {
        List<HubConfiguration> allHubs = getAllHubs();
        long activeCount = allHubs.stream().filter(HubConfiguration::isOperational).count();
        int totalVehicleCapacity = allHubs.stream()
                .filter(HubConfiguration::isOperational)
                .mapToInt(HubConfiguration::getMaxVehicles)
                .sum();

        return HubStatistics.builder()
                .totalHubs(allHubs.size())
                .activeHubs((int) activeCount)
                .totalVehicleCapacity(totalVehicleCapacity)
                .build();
    }

    /**
     * Convert AutoPlanProperties.HubConfig to domain HubConfiguration
     */
    private HubConfiguration convertToHubConfiguration(AutoPlanProperties.HubConfig config) {
        return HubConfiguration.builder()
                .id(config.getId())
                .name(config.getName())
                .location(HubConfiguration.Location.builder()
                        .latitude(config.getLocation().getLat())
                        .longitude(config.getLocation().getLon())
                        .build())
                .catchmentRadiusKm(config.getCatchmentRadiusKm())
                .maxVehicles(16) // Default, could be added to HubConfig if needed
                .active(true) // Default to active
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class HubStatistics {
        private int totalHubs;
        private int activeHubs;
        private int totalVehicleCapacity;
    }
}
