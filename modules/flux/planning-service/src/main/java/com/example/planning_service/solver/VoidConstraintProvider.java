package com.example.planning_service.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import com.example.planning_service.domain.timefold.RouteStop;
import com.example.planning_service.domain.timefold.Vehicle;

import java.math.BigDecimal;

/**
 * PROFIT FIRST Constraint Provider
 * Defines hard (must-have) and soft (optimization) constraints
 * 
 * Philosophy: Maximize profit while respecting capacity and SLA
 */
public class VoidConstraintProvider implements ConstraintProvider {

        private static final BigDecimal FUEL_COST_PER_KM = new BigDecimal("0.50"); // PLN
        private static final BigDecimal DRIVER_COST_PER_HOUR = new BigDecimal("60.00"); // PLN

        @Override
        public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
                return new Constraint[] {
                                // HARD CONSTRAINTS (must be satisfied)
                                vehicleCapacityWeight(constraintFactory),
                                slaTimeWindow(constraintFactory),
                                vehicleMaxDistance(constraintFactory),

                                // SOFT CONSTRAINTS (optimization goals)
                                maximizeProfit(constraintFactory),
                                minimizeTravelDistance(constraintFactory),
                                minimizeUnassignedOrders(constraintFactory)
                };
        }

        /**
         * HARD: Vehicle capacity (weight) must not be exceeded
         */
        protected Constraint vehicleCapacityWeight(ConstraintFactory constraintFactory) {
                return constraintFactory
                                .forEach(RouteStop.class)
                                .groupBy(RouteStop::getVehicle,
                                                ai.timefold.solver.core.api.score.stream.ConstraintCollectors
                                                                .sumBigDecimal(RouteStop::getDemandWeight))
                                .filter((vehicle, totalWeight) -> vehicle != null
                                                && totalWeight.compareTo(vehicle.getCapacityWeight()) > 0)
                                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD,
                                                (vehicle, totalWeight) -> totalWeight
                                                                .subtract(vehicle.getCapacityWeight()))
                                .asConstraint("Vehicle capacity (weight) exceeded");
        }

        /**
         * HARD: SLA time window must be respected
         */
        protected Constraint slaTimeWindow(ConstraintFactory constraintFactory) {
                return constraintFactory
                                .forEach(RouteStop.class)
                                .filter(RouteStop::isLate) // Uses isLate() method from RouteStop
                                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD,
                                                stop -> {
                                                        // Defensive null check (aligns with RouteStop.isLate logic)
                                                        if (stop.getOrder() == null
                                                                        || stop.getOrder().delivery() == null
                                                                        || stop.getOrder().delivery().sla() == null) {
                                                                return BigDecimal.ZERO; // No penalty for incomplete
                                                                                        // data
                                                        }

                                                        // Penalty proportional to delay minutes
                                                        long delayMinutes = java.time.Duration.between(
                                                                        stop.getOrder().delivery().sla(),
                                                                        stop.getArrivalTime()).toMinutes();
                                                        return BigDecimal.valueOf(delayMinutes);
                                                })
                                .asConstraint("SLA time window violated");
        }

        /**
         * HARD: Vehicle max distance (Elastic Shell Detour Limit)
         * Total distance of new stops must not exceed maxDetourKm
         */
        protected Constraint vehicleMaxDistance(ConstraintFactory constraintFactory) {
                return constraintFactory
                                .forEach(RouteStop.class)
                                .groupBy(RouteStop::getVehicle,
                                                ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sumLong(
                                                                stop -> (long) stop.getDistanceFromPreviousMeters()))
                                .filter((vehicle, totalMeters) -> vehicle != null && vehicle.getMaxDetourKm() > 0
                                                && totalMeters > vehicle.getMaxDetourKm() * 1000)
                                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD,
                                                (vehicle, totalMeters) -> BigDecimal.valueOf(
                                                                totalMeters - (vehicle.getMaxDetourKm() * 1000)))
                                .asConstraint("Max detour distance exceeded");
        }

        /**
         * SOFT: MAXIMIZE PROFIT (The Secret Sauce!)
         * Profit = Order Value - (Fuel Cost + Driver Cost)
         */
        protected Constraint maximizeProfit(ConstraintFactory constraintFactory) {
                return constraintFactory
                                .forEach(RouteStop.class)
                                .filter(stop -> stop.getVehicle() != null) // Only assigned stops
                                .rewardBigDecimal(HardSoftBigDecimalScore.ONE_SOFT,
                                                this::calculateStopProfit)
                                .asConstraint("Maximize profit");
        }

        /**
         * Calculate profit for a single stop
         * Profit = Revenue - Costs
         */
        private BigDecimal calculateStopProfit(RouteStop stop) {
                // Revenue
                BigDecimal revenue = stop.getOrderValue();

                // Cost: Fuel
                double distanceKm = stop.getDistanceFromPreviousMeters() / 1000.0;
                BigDecimal fuelCost = FUEL_COST_PER_KM.multiply(BigDecimal.valueOf(distanceKm));

                // Cost: Driver time
                long travelTimeMinutes = stop.getTravelTimeMinutes();
                BigDecimal driverCost = DRIVER_COST_PER_HOUR
                                .multiply(BigDecimal.valueOf(travelTimeMinutes))
                                .divide(BigDecimal.valueOf(60), BigDecimal.ROUND_HALF_UP);

                // Profit = Revenue - Costs
                BigDecimal profit = revenue.subtract(fuelCost).subtract(driverCost);

                return profit.max(BigDecimal.ZERO); // Never negative reward
        }

        /**
         * SOFT: Minimize total travel distance
         */
        protected Constraint minimizeTravelDistance(ConstraintFactory constraintFactory) {
                return constraintFactory
                                .forEach(RouteStop.class)
                                .filter(stop -> stop.getVehicle() != null)
                                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_SOFT,
                                                stop -> {
                                                        double distanceKm = stop.getDistanceFromPreviousMeters()
                                                                        / 1000.0;
                                                        return BigDecimal.valueOf(distanceKm).divide(
                                                                        BigDecimal.valueOf(100),
                                                                        BigDecimal.ROUND_HALF_UP);
                                                })
                                .asConstraint("Minimize travel distance");
        }

        /**
         * SOFT: Minimize unassigned orders
         * (Encourages solver to assign as many orders as possible)
         */
        protected Constraint minimizeUnassignedOrders(ConstraintFactory constraintFactory) {
                return constraintFactory
                                .forEach(RouteStop.class)
                                .filter(stop -> stop.getVehicle() == null)
                                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_SOFT,
                                                stop -> BigDecimal.valueOf(10)) // Penalty per unassigned order
                                .asConstraint("Minimize unassigned orders");
        }
}
