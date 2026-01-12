package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Manifest API endpoints.
 * Contains manifest metadata, routes, and optimization metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestResponseDto {

    private UUID id;
    private UUID driverId;
    private String driverName; // Fetched from driver service or cached
    private LocalDate date;
    private UUID vehicleId;
    private String vehicleName;
    private String status; // DRAFT, ASSIGNED, IN_PROGRESS, COMPLETED
    private double optimizationScore;
    private double totalDistanceMeters;
    private long estimatedDurationMillis;
    private String externalReference;
    private String vehiclePlate;
    private Object metadata; // JSON
    private List<RouteDto> routes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteDto {
        private UUID id;
        private UUID orderId;
        private int sequence;
        private String address;
        private String timeWindow;
        private String estimatedArrival;
        private String status; // PENDING, COMPLETED
        private double latitude;
        private double longitude;
        private String activityType; // PICKUP, DELIVERY
    }
}
