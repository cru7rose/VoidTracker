package com.example.planning_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for complete VehicleRoutingSolution serialization
 * Used by /api/planning/optimization/solution endpoint
 * 
 * Includes full Timefold metadata and chain references for advanced clients
 * Frontend can use this to reconstruct the complete solution state
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRoutingSolutionDto {

    /**
     * Unique problem identifier from solver
     */
    private String problemId;

    /**
     * Final score (HardSoftBigDecimal format)
     * Example: "0hard/962.1017soft"
     */
    private String score;

    /**
     * Number of stops not assigned to any vehicle
     */
    private Long unassignedStopsCount;

    /**
     * Total profit from all assigned orders
     */
    private BigDecimal totalProfit;

    /**
     * All vehicles (with chain references to first stops)
     */
    @JsonProperty("vehicles")
    private List<VehicleSolutionDto> vehicles;

    /**
     * All route stops (planning entities with chain info)
     */
    @JsonProperty("stops")
    private List<RouteStopSolutionDto> stops;

    /**
     * Depot/warehouse location
     */
    private LocationDto depot;

    /**
     * Problem facts: Original orders being optimized
     */
    @JsonProperty("orders")
    private List<com.example.danxils_commons.dto.OrderResponseDto> orders;

    /**
     * Solver metadata
     */
    private SolverMetadata solverMetadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolverMetadata {
        private Long solveTimeMillis;
        private Long moveEvaluationSpeed; // moves per second
        private Integer phaseCount;
        private String environmentMode;
    }
}
