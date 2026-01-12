package com.example.planning_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ARCHITEKTURA: Hub Configuration Domain Model
 * 
 * Reprezentuje centrum dystrybucyjne (hub) w sieci logistycznej.
 * Używane do regional partitioning - każdy hub obsługuje określony obszar
 * catchment.
 * 
 * Hub Types:
 * - MAIN_HUB: Główne centrum (np. Warszawa, Katowice) - duża flota
 * - MIDI_HUB: Średnie centrum regionalne (np. Poznań, Wrocław)
 * - MICRO_HUB: Małe centrum satelitarne
 * 
 * User Requirement: "musi być konfigurowalne, elastycznie z hubów"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubConfiguration {

    /**
     * Unique identifier for the hub (e.g., "HUB_WARSZAWA", "MIDI_KATOWICE")
     */
    private String id;

    /**
     * Human-readable name
     */
    private String name;

    /**
     * Hub type classification
     */
    private HubType type;

    /**
     * Hub geographic location (warehouse coordinates)
     */
    private Location location;

    /**
     * Catchment area radius in kilometers
     * Orders within this radius are assigned to this hub
     */
    private double catchmentRadiusKm;

    /**
     * Whether this hub is active for optimization
     */
    private boolean active;

    /**
     * Maximum vehicle capacity for this hub
     */
    private int maxVehicles;

    /**
     * Operating hours - shift start (e.g., "23:00")
     */
    private String shiftStartTime;

    /**
     * Operating hours - shift end (e.g., "09:00")
     */
    private String shiftEndTime;

    /**
     * Województwo/region this hub covers (optional, for reporting)
     */
    private String region;

    public enum HubType {
        MAIN_HUB, // Large distribution center (100+ vehicles)
        MIDI_HUB, // Medium regional center (20-50 vehicles)
        MICRO_HUB // Small satellite hub (5-20 vehicles)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private double latitude;
        private double longitude;

        /**
         * Calculate distance to another location using Haversine formula
         * 
         * @param other Target location
         * @return Distance in kilometers
         */
        public double distanceToKm(Location other) {
            final double EARTH_RADIUS_KM = 6371.0;

            double lat1Rad = Math.toRadians(this.latitude);
            double lat2Rad = Math.toRadians(other.latitude);
            double deltaLat = Math.toRadians(other.latitude - this.latitude);
            double deltaLon = Math.toRadians(other.longitude - this.longitude);

            double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                    + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                            * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return EARTH_RADIUS_KM * c;
        }
    }

    /**
     * Check if a location falls within this hub's catchment area
     */
    public boolean isWithinCatchmentArea(Location orderLocation) {
        if (orderLocation == null || this.location == null) {
            return false;
        }
        double distance = this.location.distanceToKm(orderLocation);
        return distance <= this.catchmentRadiusKm;
    }

    /**
     * Check if hub is operational (active + valid configuration)
     */
    public boolean isOperational() {
        return active
                && location != null
                && catchmentRadiusKm > 0
                && maxVehicles > 0;
    }
}
