package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Summary of a planned route with metrics.
 * Provides overview of route details for monitoring and reporting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSummaryDto {

    private String routeId;
    private String routeNumber;
    private String status; // PLANNED, ASSIGNED, IN_PROGRESS, COMPLETED
    private VehicleInfo vehicle;
    private DriverInfo driver;
    private RouteMetrics metrics;
    private List<String> stopPostalCodes; // For quick overview
    private LocalDateTime plannedStartTime;
    private LocalDateTime estimatedCompletionTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        private String vehicleId;
        private String registrationNumber;
        private String vehicleType;
        private Double capacityKg;
        private Double capacityM3;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverInfo {
        private String driverId;
        private String name;
        private String phoneNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteMetrics {
        private Integer totalStops;
        private Integer deliveries;
        private Integer pickups;
        private Double totalDistanceKm;
        private String estimatedDuration;
        private Double utilizationPercent; // Vehicle capacity utilization
        private List<String> zones; // Zones covered
    }

    /**
     * Check if route is in active state (assigned or in progress).
     */
    public boolean isActive() {
        return "ASSIGNED".equals(status) || "IN_PROGRESS".equals(status);
    }

    /**
     * Get efficiency score (stops per km).
     */
    public Double getEfficiencyScore() {
        if (metrics == null || metrics.getTotalDistanceKm() == null || metrics.getTotalDistanceKm() == 0) {
            return 0.0;
        }
        return metrics.getTotalStops() / metrics.getTotalDistanceKm();
    }
}
