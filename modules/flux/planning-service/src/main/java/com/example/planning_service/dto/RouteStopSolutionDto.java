package com.example.planning_service.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for RouteStop entity in full solution
 * Includes Timefold planning variables and shadow variables
 * 
 * This is DIFFERENT from the simplified RouteStopDto used in /optimize
 * endpoint!
 * This version includes chain references (previousStandstill, nextStop) for
 * traversal
 * 
 * Uses @JsonIdentityInfo to handle circular references in the chain:
 * RouteStop -> RouteStop -> ... -> null
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class RouteStopSolutionDto {

    /**
     * Stop UUID (Order ID, also used as @JsonIdentity)
     */
    private String id;

    /**
     * The order being delivered (problem fact)
     */
    @JsonProperty("order")
    private com.example.danxils_commons.dto.OrderResponseDto order;

    /**
     * Shadow variable: Anchor (vehicle) for this stop
     * Derived from traversing previousStandstill chain
     */
    @JsonProperty("vehicle")
    private String vehicleId; // ID reference

    /**
     * Demand (from order package)
     */
    private BigDecimal demandWeight;
    private BigDecimal demandVolume;

    /**
     * PLANNING VARIABLE: Previous element in route chain
     * Can be either:
     * - Vehicle ID (if this is the first stop)
     * - RouteStop ID (if this is a subsequent stop)
     * 
     * Frontend should use this to reconstruct the chain
     */
    @JsonProperty("previousStandstill")
    private StandstillReference previousStandstill;

    /**
     * Shadow variable: Next stop in chain
     * Automatically maintained by Timefold solver
     */
    @JsonProperty("nextStop")
    private String nextStopId; // ID reference to avoid infinite nesting

    /**
     * Shadow variable: Calculated arrival time
     */
    private LocalDateTime arrivalTime;

    /**
     * Location coordinates
     */
    private Double latitude;
    private Double longitude;

    /**
     * Service duration at this stop (seconds)
     */
    private Long serviceDurationSeconds;

    /**
     * Time window constraints
     */
    private LocalDateTime minStartTime;
    private LocalDateTime maxEndTime;

    /**
     * Reference to previous standstill (Vehicle or RouteStop)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StandstillReference {
        /**
         * Type: "Vehicle" or "RouteStop"
         */
        private String type;

        /**
         * ID of the referenced entity
         */
        private String id;

        /**
         * Location of the standstill (for distance calculation)
         */
        private LocationDto location;
    }
}
