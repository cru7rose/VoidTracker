package com.example.planning_service.optimization.impl;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.example.planning_service.dto.OptimizationRequestDto;
import com.example.planning_service.dto.VehicleRouteDto;
import com.example.planning_service.dto.RouteStopDto;
import com.example.planning_service.entity.FleetVehicleEntity;
import com.example.planning_service.entity.OptimizationProfileEntity;
import com.example.planning_service.optimization.VrpOptimizerService;
import com.example.planning_service.optimization.strategy.OptimizationStrategy;
import com.example.planning_service.domain.timefold.Standstill;
import com.example.planning_service.domain.timefold.Vehicle;
import com.example.planning_service.domain.timefold.RouteStop;
import com.example.planning_service.repository.FleetVehicleRepository;
import com.example.planning_service.domain.timefold.Location;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.example.planning_service.dto.OptimizationUpdateDto;
import com.example.planning_service.service.GatekeeperService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimefoldOptimizer implements OptimizationStrategy, VrpOptimizerService {

    private final SolverManager<com.example.planning_service.domain.timefold.VehicleRoutingSolution, String> solverManager;
    private final FleetVehicleRepository vehicleRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GatekeeperService gatekeeperService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /**
     * Cache for last solved solution (for /solution endpoint)
     * Using AtomicReference for thread-safe updates
     */
    private final java.util.concurrent.atomic.AtomicReference<com.example.planning_service.domain.timefold.VehicleRoutingSolution> lastSolution = new java.util.concurrent.atomic.AtomicReference<>();

    /**
     * Get the last solved solution (used by /solution endpoint)
     * 
     * @return Last VehicleRoutingSolution or null if none exists
     */
    public com.example.planning_service.domain.timefold.VehicleRoutingSolution getLastSolution() {
        return lastSolution.get();
    }

    @Override
    public List<VehicleRouteDto> optimize(OptimizationRequestDto request,
            List<com.example.danxils_commons.dto.OrderResponseDto> orders, OptimizationProfileEntity profile) {
        log.info("Starting Timefold optimization for {} orders", orders.size());

        // 1. Map to VehicleRoutingSolution (Domain)
        com.example.planning_service.domain.timefold.VehicleRoutingSolution problem = mapToProblem(request, orders);

        // 2. Solve with WebSocket broadcasting
        UUID problemId = UUID.randomUUID();
        problem.setOptimizationId(problemId.toString());

        SolverJob<com.example.planning_service.domain.timefold.VehicleRoutingSolution, String> solverJob = solverManager
                .solveBuilder()
                .withProblemId(problemId.toString())
                .withProblem(problem)
                .withBestSolutionConsumer(this::broadcastSolution)
                .run();

        com.example.planning_service.domain.timefold.VehicleRoutingSolution solution;
        try {
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Solving failed", e);
            throw new RuntimeException("Optimization failed", e);
        }

        // Cache the solution for /solution endpoint
        lastSolution.set(solution);
        log.debug("Cached solution with {} vehicles, {} stops",
                solution.getVehicles() != null ? solution.getVehicles().size() : 0,
                solution.getStops() != null ? solution.getStops().size() : 0);

        // 3. Map back to Routes
        return mapToRoutes(solution);
    }

    @Override
    public String getModelName() {
        return "Timefold";
    }

    private com.example.planning_service.domain.timefold.VehicleRoutingSolution mapToProblem(
            OptimizationRequestDto request,
            List<com.example.danxils_commons.dto.OrderResponseDto> orders) {
        // Map Vehicles
        List<Vehicle> vehicles = new ArrayList<>();
        List<FleetVehicleEntity> fleet;

        // Fetch vehicles: either specified IDs or all available
        if (request.getVehicleIds() != null && !request.getVehicleIds().isEmpty()) {
            fleet = vehicleRepository.findAllById(request.getVehicleIds());
        } else {
            // Default: Use all AVAILABLE vehicles
            fleet = vehicleRepository.findAll().stream()
                    .filter(v -> Boolean.TRUE.equals(v.getAvailable()))
                    .collect(Collectors.toList());
        }

        // Ensure at least one vehicle (fallback logic is in DispatchService, but safe
        // to check)
        if (fleet.isEmpty()) {
            fleet = vehicleRepository.findAll().stream()
                    .filter(v -> Boolean.TRUE.equals(v.getAvailable()))
                    .collect(Collectors.toList());

            if (fleet.isEmpty()) {
                log.warn("No available vehicles found for optimization!");
                // Optional: throw exception or return empty solution
            }
        }

        for (FleetVehicleEntity entity : fleet) {
            Vehicle v = new Vehicle();
            v.setId(entity.getId().toString());

            // --- Elastic Shell Logic: Adjust Capacity for Fixed Stops ---
            BigDecimal baseCapacityWeight = BigDecimal.valueOf(Math.round(entity.getCapacityWeight()));
            BigDecimal baseCapacityVolume = BigDecimal
                    .valueOf(Math.round(entity.getCapacityVolume() != null ? entity.getCapacityVolume() : 0.0));

            BigDecimal fixedUsageWeight = BigDecimal.ZERO;
            BigDecimal fixedUsageVolume = BigDecimal.ZERO;

            if (Boolean.TRUE.equals(entity.getIsFixedRoute()) && entity.getFixedStops() != null
                    && !entity.getFixedStops().isEmpty()) {
                try {
                    List<com.example.planning_service.dto.FixedStopDto> fixedStops = objectMapper.readValue(
                            entity.getFixedStops(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<com.example.planning_service.dto.FixedStopDto>>() {
                            });

                    for (com.example.planning_service.dto.FixedStopDto stop : fixedStops) {
                        if (stop.getWeight() != null)
                            fixedUsageWeight = fixedUsageWeight.add(stop.getWeight());
                        if (stop.getVolume() != null)
                            fixedUsageVolume = fixedUsageVolume.add(stop.getVolume());
                    }
                    log.info("Vehicle {} is Fixed Route. Base Cap: {}, Fixed Load: {}", v.getId(), baseCapacityWeight,
                            fixedUsageWeight);

                } catch (Exception e) {
                    log.error("Failed to parse fixed stops for vehicle {}: {}", entity.getId(), e.getMessage());
                }
            }

            // Set Remaining Capacity (Effective Capacity for Solver)
            v.setCapacityWeight(baseCapacityWeight.subtract(fixedUsageWeight).max(BigDecimal.ZERO));
            v.setCapacityVolume(baseCapacityVolume.subtract(fixedUsageVolume).max(BigDecimal.ZERO));

            // Map Elastic Shell properties
            v.setFixedRoute(Boolean.TRUE.equals(entity.getIsFixedRoute()));
            v.setMaxDetourKm(entity.getMaxDetourKm() != null ? entity.getMaxDetourKm() : 0.0);

            // Default Depot location
            v.setLocation(Location.builder()
                    .latitude(52.2297)
                    .longitude(21.0122)
                    .build());
            vehicles.add(v);
        }

        // Map Visits (Orders)
        List<RouteStop> visits = new ArrayList<>();
        for (com.example.danxils_commons.dto.OrderResponseDto order : orders) {
            // Only delivery orders usually, assuming consolidation
            // Check if delivery address exists. OrderResponseDto uses 'delivery' field.
            if (order.delivery() != null) {
                RouteStop visit = new RouteStop();
                visit.setId(order.orderId().toString());
                visit.setDemandWeight(order.aPackage() != null && order.aPackage().getWeight() != null
                        ? BigDecimal.valueOf(order.aPackage().getWeight().doubleValue())
                        : BigDecimal.ZERO);
                visit.setDemandVolume(order.aPackage() != null && order.aPackage().getVolume() != null
                        ? BigDecimal.valueOf(order.aPackage().getVolume().doubleValue())
                        : BigDecimal.ZERO);
                visit.setLatitude(order.delivery().lat() != null ? order.delivery().lat() : 0.0);
                visit.setLongitude(order.delivery().lon() != null ? order.delivery().lon() : 0.0);
                visit.setServiceDurationSeconds(300); // 5 mins default

                // Time Windows
                if (order.deliveryTimeFrom() != null) {
                    visit.setMinStartTime(LocalDateTime.ofInstant(order.deliveryTimeFrom(), ZoneId.systemDefault()));
                }
                if (order.deliveryTimeTo() != null) {
                    visit.setMaxEndTime(LocalDateTime.ofInstant(order.deliveryTimeTo(), ZoneId.systemDefault()));
                }

                visits.add(visit);
            }
        }

        // Initialize solution using setters
        com.example.planning_service.domain.timefold.VehicleRoutingSolution problem = new com.example.planning_service.domain.timefold.VehicleRoutingSolution();
        problem.setVehicles(vehicles);
        problem.setStops(visits);
        problem.setAllStops(visits); // Value
                                     // range
                                     // provider
        problem.setOrders(orders);

        // Set depot (using default location of first vehicle or Warsaw center)
        if (!vehicles.isEmpty()) {
            problem.setDepot(vehicles.get(0).getLocation());
        } else {
            problem.setDepot(Location.builder()
                    .latitude(52.2297)
                    .longitude(21.0122)
                    .build());
        }

        return problem;
    }

    public List<VehicleRouteDto> mapToRoutes(
            com.example.planning_service.domain.timefold.VehicleRoutingSolution solution) {
        List<VehicleRouteDto> routes = new ArrayList<>();

        if (solution.getVehicles() == null) {
            log.warn("Solution has no vehicles!");
            return routes;
        }

        // Build a map of PreviousStandstill ID -> NextStop
        // This allows us to traverse the chain without relying on the shadow variable
        // on Vehicle
        Map<String, RouteStop> nextStopMap = new HashMap<>();
        if (solution.getStops() != null) {
            for (RouteStop stop : solution.getStops()) {
                Standstill prev = stop.getPreviousStandstill();
                if (prev != null) {
                    String prevId = getStandstillId(prev);
                    nextStopMap.put(prevId, stop);
                }
            }
        }

        for (Vehicle vehicle : solution.getVehicles()) {
            // Find the first stop for this vehicle
            RouteStop visit = nextStopMap.get(vehicle.getId());

            if (visit == null) {
                // Log unused if needed, but skip
                continue;
            }

            List<RouteStopDto> stops = new ArrayList<>();
            while (visit != null) {
                RouteStopDto stop = RouteStopDto.builder()
                        .stopId(visit.getId())
                        .type("DELIVERY")
                        .lat(visit.getLatitude())
                        .lon(visit.getLongitude())
                        .orderId(UUID.fromString(visit.getId()))
                        .plannedArrival(visit.getArrivalTime() != null
                                ? visit.getArrivalTime().atZone(ZoneId.systemDefault()).toInstant()
                                : null)
                        .build();

                stops.add(stop);

                // Move to next
                visit = nextStopMap.get(visit.getId());
            }

            if (!stops.isEmpty()) {
                VehicleRouteDto route = new VehicleRouteDto();
                route.setRouteId(UUID.randomUUID().toString());
                route.setVehicleId(UUID.fromString(vehicle.getId()));
                route.setStops(stops);
                route.setTotalDistance(0L);
                route.setTotalTime(0L);
                routes.add(route);
            }
        }
        return routes;
    }

    private String getStandstillId(Standstill standstill) {
        if (standstill instanceof Vehicle) {
            return ((Vehicle) standstill).getId();
        } else if (standstill instanceof RouteStop) {
            return ((RouteStop) standstill).getId();
        }
        return null; // Should not happen
    }

    @Override
    public VrpOptimizerService.VehicleRoutingSolution calculateRoutes(
            List<com.example.danxils_commons.dto.OrderResponseDto> ordersToPlan,
            List<FleetVehicleEntity> fleet,
            OptimizationProfileEntity profile) {
        // Create Request DTO
        OptimizationRequestDto request = new OptimizationRequestDto();
        request.setVehicleIds(fleet.stream().map(FleetVehicleEntity::getId).collect(Collectors.toList()));

        // Optimize
        List<VehicleRouteDto> dtos = optimize(request, ordersToPlan, profile);

        // Map to VehicleRoutingSolution
        List<VrpOptimizerService.VehicleRoutingSolution.Route> routes = dtos.stream().map(dto -> {
            List<VrpOptimizerService.VehicleRoutingSolution.Activity> activities = dto.getStops().stream()
                    .map(stop -> new VrpOptimizerService.VehicleRoutingSolution.Activity(
                            stop.getOrderId(),
                            VrpOptimizerService.VehicleRoutingSolution.ActivityType.DELIVERY,
                            stop.getLat(),
                            stop.getLon(),
                            stop.getPlannedArrival() != null ? stop.getPlannedArrival().toEpochMilli() : 0,
                            stop.getPlannedArrival() != null ? stop.getPlannedArrival().plusSeconds(300).toEpochMilli()
                                    : 0))
                    .collect(Collectors.toList());

            return new VrpOptimizerService.VehicleRoutingSolution.Route(
                    dto.getVehicleId(),
                    activities,
                    (double) dto.getTotalDistance(),
                    dto.getTotalTime());
        }).collect(Collectors.toList());

        return new VrpOptimizerService.VehicleRoutingSolution(routes);
    }

    @Override
    public List<RouteStopDto> resequenceRoutes(List<RouteStopDto> stops, double startLat, double startLon) {
        log.info("Resequencing {} stops starting from {}, {}", stops.size(), startLat, startLon);
        List<RouteStopDto> optimized = new ArrayList<>();
        List<RouteStopDto> remaining = new ArrayList<>(stops);

        double currentLat = startLat;
        double currentLon = startLon;

        while (!remaining.isEmpty()) {
            RouteStopDto nearest = null;
            double minDist = Double.MAX_VALUE;

            for (RouteStopDto candidate : remaining) {
                double dist = calculateHaversineDistance(currentLat, currentLon, candidate.getLat(),
                        candidate.getLon());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = candidate;
                }
            }

            if (nearest != null) {
                optimized.add(nearest);
                remaining.remove(nearest);
                currentLat = nearest.getLat();
                currentLon = nearest.getLon();
            } else {
                break; // Should not happen
            }
        }

        return optimized;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters
    }

    private void broadcastSolution(com.example.planning_service.domain.timefold.VehicleRoutingSolution solution) {
        try {
            OptimizationUpdateDto update = new OptimizationUpdateDto();
            update.setScore(solution.getScore() != null ? solution.getScore().toString() : "N/A");
            update.setSolverStatus("SOLVING"); // You could pass SolverStatus if available from event

            // Gatekeeper Validation
            GatekeeperService.SafetyReport report = gatekeeperService.validateSolution(solution, lastSolution.get());
            update.setRequiresApproval(report.isRequiresApproval());
            update.setWarnings(report.getWarnings());

            // Map routes for visualization
            List<VehicleRouteDto> routeDtos = mapToRoutes(solution);
            List<OptimizationUpdateDto.RouteDto> visualRoutes = routeDtos.stream().map(dto -> {
                OptimizationUpdateDto.RouteDto r = new OptimizationUpdateDto.RouteDto();
                r.setVehicleId(dto.getVehicleId().toString());
                r.setTotalDistanceMeters(dto.getTotalDistance());

                // Map stops to points
                List<OptimizationUpdateDto.PointDto> points = new ArrayList<>();
                // Flatten: Depot -> Stop1 -> Stop2 ...
                // Note: VehicleRouteDto is simplified, doesn't always have explicit start/end
                // depot in path list
                // For "High Fidelity", we need correct lat/lons.

                // We'll trust the stops list for now.
                // Ideally we'd get the vehicle depot from the solution, but mapToRoutes loses
                // that context slightly.
                // Let's iterate stops.
                for (RouteStopDto stop : dto.getStops()) {
                    points.add(new OptimizationUpdateDto.PointDto(
                            stop.getLat(), stop.getLon(), "STOP", stop.getOrderId()));
                }
                r.setPath(points);
                r.setColor(getColorForVehicle(r.getVehicleId()));
                return r;
            }).collect(Collectors.toList());

            update.setRoutes(visualRoutes);

            messagingTemplate.convertAndSend("/topic/optimization-updates", update);
        } catch (Exception e) {
            log.warn("Failed to broadcast solution update: {}", e.getMessage());
        }
    }

    private String getColorForVehicle(String vehicleId) {
        // Deterministic color generation
        int hash = vehicleId.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = (hash & 0x0000FF);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
