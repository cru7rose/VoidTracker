package com.example.planning_service.domain.timefold;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import com.example.danxils_commons.dto.OrderResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Planning Entity - Represents a delivery stop in a route
 * This is the variable that Timefold Solver moves around to optimize routes
 */
@PlanningEntity(difficultyComparatorClass = RouteStopDifficultyComparator.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStop extends Standstill {

    @PlanningId
    private String id;

    /**
     * The order being delivered (Problem Fact)
     */
    private OrderResponseDto order;

    /**
     * Shadow variable: The vehicle that this stop belongs to
     */
    @ai.timefold.solver.core.api.domain.variable.AnchorShadowVariable(sourceVariableName = "previousStandstill")
    private Vehicle vehicle;

    /**
     * Demand / Size of the order
     */
    private BigDecimal demandWeight;
    private BigDecimal demandVolume;

    /**
     * PLANNING VARIABLE: Previous stop in the route chain
     * Can be a Vehicle (start of route) or another RouteStop
     */
    @PlanningVariable(valueRangeProviderRefs = { "vehicleRange",
            "stopRange" }, graphType = ai.timefold.solver.core.api.domain.variable.PlanningVariableGraphType.CHAINED)
    private Standstill previousStandstill;

    // nextStop is inherited from Standstill

    /**
     * Calculated arrival time at this stop (shadow variable)
     */
    @ShadowVariable(variableListenerClass = ArrivalTimeUpdatingVariableListener.class, sourceVariableName = "previousStandstill")
    private LocalDateTime arrivalTime;

    /**
     * Location coordinates (Problem Fact)
     */
    private Double latitude;
    private Double longitude;

    /**
     * Service duration in seconds
     */
    private long serviceDurationSeconds;

    /**
     * Time Window Start
     */
    private LocalDateTime minStartTime;

    /**
     * Time Window End
     */
    private LocalDateTime maxEndTime;

    /**
     * Get location of this stop
     */
    @Override
    public Location getLocation() {
        if (latitude != null && longitude != null) {
            return Location.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
        }
        // Fallback to order if not set directly
        if (order == null || order.delivery() == null) {
            return null;
        }
        return Location.builder()
                .latitude(order.delivery().lat())
                .longitude(order.delivery().lon())
                .city(order.delivery().city())
                .build();
    }

    // Compatibility methods for TimefoldOptimizer
    public Double getLatitude() {
        Location loc = getLocation();
        return loc != null ? loc.getLatitude() : null;
    }

    public Double getLongitude() {
        Location loc = getLocation();
        return loc != null ? loc.getLongitude() : null;
    }

    public Standstill getNextStandstill() {
        return nextStop;
    }

    /**
     * Calculate distance from previous stop (in meters)
     */
    public double getDistanceFromPreviousMeters() {
        if (previousStandstill == null) {
            return 0.0;
        }
        Location prev = previousStandstill.getLocation();
        Location current = getLocation();

        if (prev == null || current == null) {
            return 0.0;
        }

        return calculateHaversineDistance(prev, current);
    }

    /**
     * Calculate travel time from previous stop (in minutes)
     */
    public long getTravelTimeMinutes() {
        double distanceKm = getDistanceFromPreviousMeters() / 1000.0;
        double avgSpeedKmh = 40.0; // Average urban speed
        return Math.round((distanceKm / avgSpeedKmh) * 60.0);
    }

    /**
     * Haversine formula for distance calculation
     */
    private static double calculateHaversineDistance(Location from, Location to) {
        double R = 6371000; // Earth radius in meters
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double dLat = Math.toRadians(to.getLatitude() - from.getLatitude());
        double dLon = Math.toRadians(to.getLongitude() - from.getLongitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Get order value (for profit calculation)
     */
    public BigDecimal getOrderValue() {
        // TODO: Implement EAV properties lookup or use PackageDto value
        // For now, return default value
        return BigDecimal.valueOf(100.0);
    }

    /**
     * Check if this stop violates SLA
     */
    public boolean isLate() {
        if (arrivalTime == null) {
            return false;
        }
        if (maxEndTime != null) {
            return arrivalTime.isAfter(maxEndTime);
        }
        if (order == null || order.delivery() == null || order.delivery().sla() == null) {
            return false;
        }
        return arrivalTime
                .isAfter(java.time.LocalDateTime.ofInstant(order.delivery().sla(), java.time.ZoneId.systemDefault()));
    }
}
