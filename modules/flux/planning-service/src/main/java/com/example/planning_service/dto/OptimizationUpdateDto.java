package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationUpdateDto {
    private String solverStatus; // SOLVING, FINISHED_EARLY, ETC
    private String score;
    private long scoreCalculationCount;
    private List<RouteDto> routes;

    // Gatekeeper Approval
    private Boolean requiresApproval;
    private List<String> warnings;

    // Simplified Internal DTO for Map Visualization
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RouteDto {
        private String vehicleId;
        private List<PointDto> path;
        private String color; // For visual distinction
        private double totalDistanceMeters;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PointDto {
        private double lat;
        private double lon;
        private String type; // "DEPOT", "STOP"
        private UUID visitId; // Order ID if stop
    }
}
