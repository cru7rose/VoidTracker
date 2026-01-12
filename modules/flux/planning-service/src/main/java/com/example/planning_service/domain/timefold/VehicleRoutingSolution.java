package com.example.planning_service.domain.timefold;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import com.example.danxils_commons.dto.OrderResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Planning Solution - The complete vehicle routing problem
 * Contains all entities (stops) and facts (vehicles, orders)
 */
@PlanningSolution
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRoutingSolution {

    /**
     * All stops to be scheduled (PLANNING ENTITIES)
     */
    @PlanningEntityCollectionProperty
    private List<RouteStop> stops;

    /**
     * Available vehicles (VALUE RANGE for vehicle assignment)
     */
    @ValueRangeProvider(id = "vehicleRange")
    @ProblemFactCollectionProperty
    private List<Vehicle> vehicles;

    /**
     * All stops (VALUE RANGE for chained variable)
     */
    @ValueRangeProvider(id = "stopRange")
    private List<RouteStop> allStops;

    /**
     * Orders being delivered (PROBLEM FACTS)
     */
    @ProblemFactCollectionProperty
    private List<OrderResponseDto> orders;

    /**
     * Warehouse/depot location (PROBLEM FACT)
     */
    @ProblemFactProperty
    private Location depot;

    /**
     * PLANNING SCORE (hard/soft constraints evaluation)
     */
    @PlanningScore
    private HardSoftBigDecimalScore score;

    /**
     * Optimization ID for tracking
     */
    private String optimizationId;

    /**
     * Get unassigned stops count
     */
    public long getUnassignedStopsCount() {
        return stops != null
                ? stops.stream().filter(stop -> stop.getVehicle() == null).count()
                : 0;
    }

    /**
     * Get total profit (sum of all assigned orders)
     */
    public java.math.BigDecimal getTotalProfit() {
        if (stops == null) {
            return java.math.BigDecimal.ZERO;
        }

        return stops.stream()
                .filter(stop -> stop.getVehicle() != null)
                .map(RouteStop::getOrderValue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
