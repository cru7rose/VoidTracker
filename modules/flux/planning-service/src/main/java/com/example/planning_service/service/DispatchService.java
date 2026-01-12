package com.example.planning_service.service;

import com.example.planning_service.entity.OptimizationSolutionEntity;
import com.example.planning_service.optimization.VrpOptimizerService;
import com.example.planning_service.repository.OptimizationSolutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DispatchService {

    private final OptimizationSolutionRepository solutionRepository;
    private final ObjectMapper objectMapper;
    private final com.example.planning_service.repository.FleetVehicleRepository vehicleRepository;

    @Transactional
    public void assignDriverToVehicle(UUID vehicleId, String driverId) {
        com.example.planning_service.entity.FleetVehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        vehicle.setDriverId(driverId);
        vehicleRepository.save(vehicle);
    }

    /**
     * Manually assigns an order to a specific route by appending it to the end.
     * Creates a NEW solution version to persist the change.
     */
    @Transactional
    public VrpOptimizerService.VehicleRoutingSolution assignOrderToRoute(String routeId, String orderId) {
        // 1. Fetch latest solution
        OptimizationSolutionEntity latestEntity = solutionRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new RuntimeException("No active plan found to modify"));

        try {
            String jsonString = objectMapper.writeValueAsString(latestEntity.getSolutionJson());
            VrpOptimizerService.VehicleRoutingSolution currentSolution = objectMapper.readValue(
                    jsonString,
                    VrpOptimizerService.VehicleRoutingSolution.class);

            // 2. Modify the solution
            List<VrpOptimizerService.VehicleRoutingSolution.Route> modifiedRoutes = new ArrayList<>();
            boolean routeFound = false;

            for (VrpOptimizerService.VehicleRoutingSolution.Route route : currentSolution.routes()) {
                // Robust ID matching: Check exact match or UUID match
                String vId = route.vehicleId() != null ? route.vehicleId().toString() : "null";

                if (routeId != null && routeId.equalsIgnoreCase(vId)) {
                    routeFound = true;
                    List<VrpOptimizerService.VehicleRoutingSolution.Activity> newActivities = new ArrayList<>(route.activities());

                    // Add the new activity
                    newActivities.add(new VrpOptimizerService.VehicleRoutingSolution.Activity(
                            UUID.fromString(orderId),
                            VrpOptimizerService.VehicleRoutingSolution.ActivityType.DELIVERY,
                            0.0, 0.0, // TODO: Fetch real lat/lon if possible
                            0, 0));

                    modifiedRoutes.add(new VrpOptimizerService.VehicleRoutingSolution.Route(
                            route.vehicleId(),
                            newActivities,
                            route.totalDistance(),
                            route.totalTimeMillis()));
                } else {
                    modifiedRoutes.add(route);
                }
            }

            if (!routeFound) {
                throw new RuntimeException("Route not found: " + routeId);
            }

            VrpOptimizerService.VehicleRoutingSolution newSolution = new VrpOptimizerService.VehicleRoutingSolution(modifiedRoutes);

            // 3. Save as new version
            // 3. Save as new version
            OptimizationSolutionEntity newEntity = OptimizationSolutionEntity.builder()
                    .id(UUID.randomUUID())
                    .status(OptimizationSolutionEntity.Status.DRAFT) // Keep as DRAFT until published
                    .createdAt(java.time.Instant.now())
                    .solutionJson(newSolution)
                    .build();

            solutionRepository.save(newEntity);

            return newSolution;

        } catch (Exception e) {
            log.error("Failed to parse or modify solution", e);
            throw new RuntimeException("Failed to modify plan", e);
        }
    }

    @Transactional
    public void resetPlan() {
        // Soft delete or just create an empty solution?
        // For MVP, we just create a new empty solution.
        OptimizationSolutionEntity emptyEntity = OptimizationSolutionEntity.builder()
                .id(UUID.randomUUID())
                .status(OptimizationSolutionEntity.Status.DRAFT)
                .createdAt(java.time.Instant.now())
                .solutionJson(new VrpOptimizerService.VehicleRoutingSolution(new ArrayList<>()))
                .build();
        solutionRepository.save(emptyEntity);
    }
}
