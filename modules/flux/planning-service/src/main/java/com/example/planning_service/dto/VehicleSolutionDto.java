package com.example.planning_service.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

/**
 * DTO for Vehicle entity in full solution
 * Includes reference to first stop in chain (nextVisit)
 * 
 * Uses @JsonIdentityInfo to handle circular references:
 * Vehicle -> RouteStop -> Vehicle (via previousStandstill)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class VehicleSolutionDto {

    /**
     * Vehicle UUID (also used as @JsonIdentity)
     */
    private String id;

    /**
     * Vehicle display name
     */
    private String name;

    /**
     * Starting location (depot/warehouse)
     */
    private LocationDto location;

    /**
     * Capacity constraints
     */
    private BigDecimal capacityWeight; // kg
    private BigDecimal capacityVolume; // m³

    /**
     * Special capabilities (e.g., "REFRIGERATED", "HAZMAT")
     */
    private Set<String> skillSet;

    /**
     * Driver assignment
     */
    private String driverId;
    private String driverName;

    /**
     * Availability status
     */
    private Boolean available;

    /**
     * Shadow variable: First stop in this vehicle's route chain
     * This is the entry point for traversing the route
     * 
     * References RouteStopSolutionDto by @JsonIdentity
     */
    @JsonProperty("nextVisit")
    private String nextStopId; // ID reference to avoid full object nesting
}
