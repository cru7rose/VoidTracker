package com.example.planning_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ARCHITEKTURA: Geographic Location DTO
 * 
 * Reprezentuje lokalizację geograficzną (lat/lon) używaną w:
 * - Orders (delivery locations)
 * - Hubs (depot coordinates)
 * - Vehicles (current position for real-time re-routing)
 * 
 * Used by: Google Maps API, Timefold VRP, Regional Partitioning
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private double latitude;
    private double longitude;

    /**
     * Optional: location name for debugging
     */
    private String name;

    /**
     * Calculate straight-line (Haversine) distance to another location
     * 
     * @param other Target location
     * @return Distance in kilometers
     */
    public double straightLineDistanceKm(Location other) {
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

    /**
     * Format as Google Maps API coordinate string
     * 
     * @return "lat,lon" format
     */
    public String toGoogleMapsFormat() {
        return String.format("%.6f,%.6f", latitude, longitude);
    }

    @Override
    public String toString() {
        if (name != null) {
            return String.format("%s (%.4f, %.4f)", name, latitude, longitude);
        }
        return String.format("(%.4f, %.4f)", latitude, longitude);
    }
}
