package com.example.planning_service.service;

import com.example.planning_service.client.OrderServiceClient;
import com.example.planning_service.optimization.VrpOptimizerService.VehicleRoutingSolution;
import com.example.planning_service.optimization.VrpOptimizerService.VehicleRoutingSolution.Route;
import com.example.planning_service.optimization.VrpOptimizerService.VehicleRoutingSolution.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ENTERPRISE SERVICE: Driver Assignment Enrichment
 * 
 * Enriches VRP optimization solutions with driver assignment data from
 * order-service.
 * Implements enterprise-level data aggregation pattern with fallback handling.
 */
@Slf4j
@Service
public class DriverEnrichmentService {

    private final OrderServiceClient orderServiceClient;

    public DriverEnrichmentService(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    /**
     * Enriches VRP solution routes with driver assignments.
     * 
     * For each route:
     * 1. Extracts all orderIds from route activities
     * 2. Queries order-service for driver assignments in bulk
     * 3. Determines route driver (most common driver across orders)
     * 4. Creates enriched route with driverId populated
     * 
     * @param solution Original VRP solution
     * @return Enriched solution with driver assignments
     */
    public VehicleRoutingSolution enrichWithDriverAssignments(VehicleRoutingSolution solution) {
        if (solution == null || solution.routes() == null) {
            return solution;
        }

        List<Route> enrichedRoutes = solution.routes().stream()
                .map(this::enrichRoute)
                .collect(Collectors.toList());

        return new VehicleRoutingSolution(enrichedRoutes);
    }

    private Route enrichRoute(Route route) {
        if (route.activities() == null || route.activities().isEmpty()) {
            return route;
        }

        // Extract orderIds from route activities
        List<UUID> orderIds = route.activities().stream()
                .map(Activity::orderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return route;
        }

        try {
            // Bulk query order-service for driver assignments
            Map<String, String> assignments = orderServiceClient.getDriverAssignments(orderIds);

            if (assignments.isEmpty()) {
                return route;
            }

            // Determine route driver - use most common driver
            // (In properly assigned routes, all orders should have same driver)
            String routeDriver = findMostCommonDriver(assignments);

            // Create enriched route with driverId
            return new Route(
                    route.vehicleId(),
                    route.activities(),
                    route.totalDistance(),
                    route.totalTimeMillis(),
                    routeDriver);

        } catch (Exception e) {
            log.warn("Failed to enrich route {} with driver data: {}",
                    route.vehicleId(), e.getMessage());
            return route;
        }
    }

    /**
     * Determines most common driver from assignment map.
     * Uses frequency counting to handle edge cases.
     */
    private String findMostCommonDriver(Map<String, String> assignments) {
        return assignments.values().stream()
                .collect(Collectors.groupingBy(
                        driverId -> driverId,
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
