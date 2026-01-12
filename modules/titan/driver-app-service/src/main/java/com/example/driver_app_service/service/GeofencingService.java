package com.example.driver_app_service.service;

import com.example.driver_app_service.dto.ScanRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeofencingService {
    
    // Constant threshold in meters
    private static final double GEOFENCE_THRESHOLD_METERS = 300.0;

    public void validateGeofence(ScanRequestDto request) {
        // 1. Fetch Target Location from Order Service (Mocked for MVP)
        // In real impl, use Feign Client to get Order details by barcode
        double targetLat = 52.2297; // Example: Warsaw Center
        double targetLon = 21.0122;
        
        // Mock logic: some barcodes are "far away" for testing
        if ("FAR_AWAY_PACKAGE".equals(request.getBarcode())) {
            targetLat = 50.0;
            targetLon = 20.0;
        }

        double distance = calculateHaversineDistance(request.getLat(), request.getLon(), targetLat, targetLon);
        log.info("Geofence Check: Order={}, Driver=({}, {}), Target=({}, {}), Distance={}m", 
                request.getBarcode(), request.getLat(), request.getLon(), targetLat, targetLon, distance);

        if (distance > GEOFENCE_THRESHOLD_METERS) {
            if (request.isOverride()) {
                log.warn("Geofence BREACH IGNORED by Override for Order {}", request.getBarcode());
                // Emit "Force Acceptance" event
            } else {
                log.error("Geofence BREACH for Order {}. Distance: {}m", request.getBarcode(), distance);
                throw new GeofenceException("Jesteś za daleko od celu (" + (int)distance + "m). Wymagane < 300m.");
            }
        } else {
            log.info("Geofence VALID for Order {}", request.getBarcode());
        }
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return distance;
    }
    
    public static class GeofenceException extends RuntimeException {
        public GeofenceException(String message) {
            super(message);
        }
    }
}
