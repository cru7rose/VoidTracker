package com.example.planning_service.domain.timefold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Vehicle - Problem Fact (not changed by solver, only referenced)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(callSuper = false)
public class Vehicle extends Standstill {

    private String id;
    private String name;

    /**
     * Starting location (depot/warehouse)
     */
    private Location location;

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
     * Driver assignment (if any)
     */
    private String driverId;
    private String driverName;

    /**
     * Availability status
     */
    private boolean available;

    // --- Elastic Shell (Milkrun) ---
    private boolean isFixedRoute;
    private double maxDetourKm;

    // Shadow Variable: Capacity remaining after fixed stops
    // In MVP this is pre-calculated during mapping, but ideally updated by
    // listeners
    private BigDecimal remainingCapacityWeight;
    private BigDecimal remainingCapacityVolume;

    /**
     * Shadow Variable: Available time window for ad-hoc stops
     * Calculated based on fixed stops and assigned stops
     * Updated by AvailableTimeWindowUpdatingVariableListener
     */
    @ai.timefold.solver.core.api.domain.variable.ShadowVariable(variableListenerClass = AvailableTimeWindowUpdatingVariableListener.class, sourceVariableName = "nextStop")
    private java.time.LocalDateTime availableTimeWindowStart;
    private java.time.LocalDateTime availableTimeWindowEnd;

    // nextStop is inherited from Standstill

    /**
     * Get total weight of assigned stops
     */
    public BigDecimal getTotalAssignedWeight(java.util.List<RouteStop> allStops) {
        // TODO: Implement EAV property access for weight
        // For now, return default weight per package
        long assignedCount = allStops.stream()
                .filter(stop -> stop.getVehicle() != null && stop.getVehicle().getId().equals(this.id))
                .count();
        return BigDecimal.valueOf(assignedCount * 10); // 10kg average
    }

    /**
     * Check if vehicle can handle a specific order (skill matching)
     */
    public boolean canHandle(com.example.danxils_commons.dto.OrderResponseDto order) {
        // TODO: Implement skill matching with EAV properties
        // For now, all vehicles can handle all orders
        return true;
    }

    @Override
    public Location getLocation() {
        return location;
    }
}
