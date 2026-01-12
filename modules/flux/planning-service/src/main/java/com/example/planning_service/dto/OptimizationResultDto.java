package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Detailed result of route optimization process.
 * Provides transparency into optimization metrics and solution quality.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationResultDto {

    private String status; // SUCCESS, TIMEOUT, FAILED, NO_FEASIBLE_SOLUTION
    private String solutionScore; // TimeFold score (e.g., "0hard/-2500soft")
    private Integer routesGenerated;
    private Integer ordersPlanned;
    private Integer unassignedOrders;
    private Double totalDistanceKm;
    private String totalDuration; // Human-readable (e.g., "4h 30m")
    private Long optimizationTimeMs;
    private List<String> warnings;
    private List<String> violations; // Hard constraint violations
    private OptimizationConfig config;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationConfig {
        private Integer maxIterations;
        private Integer timeLimitSeconds;
        private Integer vehicleCount;
        private String profileName;
    }

    /**
     * Check if optimization was fully successful.
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status) && (unassignedOrders == null || unassignedOrders == 0);
    }

    /**
     * Get average distance per route.
     */
    public Double getAverageDistancePerRoute() {
        if (routesGenerated == null || routesGenerated == 0 || totalDistanceKm == null) {
            return 0.0;
        }
        return totalDistanceKm / routesGenerated;
    }
}
