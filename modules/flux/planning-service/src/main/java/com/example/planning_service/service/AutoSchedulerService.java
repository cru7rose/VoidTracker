package com.example.planning_service.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.planning_service.domain.timefold.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Auto-Scheduler Service - Orchestrates continuous route optimization
 * Triggers solving when new orders arrive (via Kafka events)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutoSchedulerService {

    private final SolverManager<VehicleRoutingSolution, UUID> solverManager;

    // Repositories (inject as needed)
    // private final VehicleRepository vehicleRepository;
    // private final OrderRepository orderRepository;

    private static final UUID PROBLEM_ID = UUID.randomUUID();

    /**
     * Trigger route optimization
     * Called when new order arrives or route needs re-optimization
     */
    public void triggerOptimization(List<OrderResponseDto> orders) {
        log.info("[AUTO-SCHEDULER] Triggering optimization for {} orders", orders.size());

        // Build problem
        VehicleRoutingSolution problem = buildProblem(orders);

        // Solve asynchronously (non-blocking)
        solverManager.solveAndListen(
                PROBLEM_ID,
                problemId -> problem,
                this::onBestSolutionChanged,
                (problemId, error) -> log.error("[AUTO-SCHEDULER] Solving failed", error));

        log.info("[AUTO-SCHEDULER] Solver started asynchronously");
    }

    /**
     * Build optimization problem from current state
     */
    private VehicleRoutingSolution buildProblem(List<OrderResponseDto> orders) {
        log.info("[AUTO-SCHEDULER] Building problem with {} orders", orders.size());

        // Create vehicles (mock data - replace with DB query)
        List<Vehicle> vehicles = createMockVehicles();

        // Create route stops from orders
        List<RouteStop> stops = orders.stream()
                .map(order -> RouteStop.builder()
                        .id(order.orderId().toString())
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        // Build solution
        return VehicleRoutingSolution.builder()
                .optimizationId(PROBLEM_ID.toString())
                .stops(stops)
                .allStops(stops) // Same list for value range
                .vehicles(vehicles)
                .orders(orders)
                .depot(createWarehouseLocation())
                .build();
    }

    /**
     * Called every time solver finds a better solution
     */
    private void onBestSolutionChanged(VehicleRoutingSolution solution) {
        if (solution.getScore() == null) {
            return;
        }

        log.info("[AUTO-SCHEDULER] 🔥 New best solution! Score: {}, Unassigned: {}, Profit: {} PLN",
                solution.getScore(),
                solution.getUnassignedStopsCount(),
                solution.getTotalProfit());

        // TODO: Publish to frontend via WebSocket
        // TODO: Persist intermediate solution if feasible

        if (solution.getScore().isFeasible()) {
            log.info("[AUTO-SCHEDULER] ✅ Solution is FEASIBLE");
        } else {
            log.warn("[AUTO-SCHEDULER] ⚠️ Solution violates hard constraints");
        }
    }

    /**
     * Called when solving completes
     */
    private void onSolvingEnded(VehicleRoutingSolution finalSolution) {
        log.info("[AUTO-SCHEDULER] 🎯 Solving COMPLETED. Final score: {}", finalSolution.getScore());

        if (!finalSolution.getScore().isFeasible()) {
            log.error("[AUTO-SCHEDULER] ❌ No feasible solution found! Check constraints.");
            return;
        }

        log.info("[AUTO-SCHEDULER] Final stats:");
        log.info("  - Total stops: {}", finalSolution.getStops().size());
        log.info("  - Unassigned: {}", finalSolution.getUnassignedStopsCount());
        log.info("  - Total profit: {} PLN", finalSolution.getTotalProfit());

        // TODO: Persist final solution to DB
        // TODO: Publish driver assignments to Kafka
        // TODO: Notify frontend via WebSocket
    }

    /**
     * Create mock vehicles (replace with DB query)
     */
    private List<Vehicle> createMockVehicles() {
        return List.of(
                Vehicle.builder()
                        .id("VEH-001")
                        .name("Sprinter 1")
                        .location(createWarehouseLocation())
                        .capacityWeight(new BigDecimal("1000"))
                        .capacityVolume(new BigDecimal("15"))
                        .available(true)
                        .build(),
                Vehicle.builder()
                        .id("VEH-002")
                        .name("Sprinter 2")
                        .location(createWarehouseLocation())
                        .capacityWeight(new BigDecimal("1000"))
                        .capacityVolume(new BigDecimal("15"))
                        .available(true)
                        .build());
    }

    /**
     * Create warehouse location (mock)
     */
    private Location createWarehouseLocation() {
        return Location.builder()
                .latitude(52.2297)
                .longitude(21.0122)
                .city("Warsaw")
                .address("Warehouse Main")
                .build();
    }

    /**
     * Stop solver (for cleanup)
     */
    public void stopSolver() {
        log.info("[AUTO-SCHEDULER] Stopping solver");
        solverManager.terminateEarly(PROBLEM_ID);
    }
}
