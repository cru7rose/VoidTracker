package com.example.planning_service.service;

import com.example.planning_service.entity.LocationPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class TravelTimeService {

    @Value("${google.maps.api-key:}")
    private String googleMapsApiKey;

    private static final double AVERAGE_SPEED_KMH = 50.0; // Fallback speed

    /**
     * Calculates travel time between two points.
     * Uses Haversine fallback if Google Maps API key is missing.
     */
    public Duration calculateTravelTime(LocationPoint origin, LocationPoint destination) {
        if (origin == null || destination == null || origin.getCoordinates() == null
                || destination.getCoordinates() == null) {
            return Duration.ZERO;
        }

        if (googleMapsApiKey != null && !googleMapsApiKey.isEmpty()) {
            // TODO: Implement Google Maps Distance Matrix API call here
            log.debug("Google Maps API Key present, but integration not yet implemented. Using Haversine fallback.");
        }

        return calculateHaversineTravelTime(origin, destination);
    }

    private Duration calculateHaversineTravelTime(LocationPoint origin, LocationPoint destination) {
        double lat1 = origin.getCoordinates().getY();
        double lon1 = origin.getCoordinates().getX();
        double lat2 = destination.getCoordinates().getY();
        double lon2 = destination.getCoordinates().getX();

        double distanceKm = haversine(lat1, lon1, lat2, lon2);
        double hours = distanceKm / AVERAGE_SPEED_KMH;

        return Duration.ofMinutes((long) (hours * 60));
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
