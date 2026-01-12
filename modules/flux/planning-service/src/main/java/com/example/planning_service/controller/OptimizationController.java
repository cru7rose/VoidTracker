package com.example.planning_service.controller;

import com.example.planning_service.dto.ResequenceRequest;
import com.example.planning_service.dto.RouteStopDto;
import com.example.planning_service.optimization.VrpOptimizerService;
import com.example.planning_service.optimization.VrpOptimizerService.VehicleRoutingSolution;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.planning_service.client.OrderServiceClient;
import com.example.planning_service.entity.FleetVehicleEntity;
import com.example.planning_service.repository.FleetVehicleRepository;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;

@RestController
@RequestMapping("/optimization")
@RequiredArgsConstructor
@Slf4j
public class OptimizationController {

    private final VrpOptimizerService vrpOptimizerService;
    private final OrderServiceClient orderServiceClient;
    private final FleetVehicleRepository vehicleRepository;
    private final com.example.planning_service.repository.WebhookConfigRepository webhookConfigRepository;
    private final com.example.planning_service.repository.DriverTaskRepository driverTaskRepository;
    private final com.example.planning_service.repository.OptimizationSolutionRepository solutionRepository;
    private final com.example.planning_service.repository.CommunicationLogRepository communicationLogRepository;
    private final com.example.planning_service.service.OptimizationProfileService optimizationProfileService;
    private final com.example.planning_service.service.ManifestService manifestService;
    private final com.example.planning_service.service.DriverEnrichmentService driverEnrichmentService;

    private final com.example.planning_service.repository.CarrierComplianceRepository carrierComplianceRepository;
    private final com.example.planning_service.repository.VehicleProfileRepository vehicleProfileRepository;

    @PostMapping("/resequence")
    public ResponseEntity<List<RouteStopDto>> resequenceRoute(@RequestBody ResequenceRequest request) {
        if (request.getStops() == null || request.getStops().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<RouteStopDto> optimizedStops = vrpOptimizerService.resequenceRoutes(
                request.getStops(),
                request.getCurrentLat(),
                request.getCurrentLng());

        return ResponseEntity.ok(optimizedStops);
    }

    @PostMapping("/optimize")
    public ResponseEntity<VrpOptimizerService.VehicleRoutingSolution> optimizeRoutes(
            @RequestBody com.example.planning_service.dto.OptimizationRequestDto request) {
        log.info("Received optimization request for {} orders and {} vehicles",
                request.getOrderIds() != null ? request.getOrderIds().size() : 0,
                request.getVehicleIds() != null ? request.getVehicleIds().size() : 0);

        // 1. Fetch Orders
        List<OrderResponseDto> orders = new ArrayList<>();
        if (request.getOrderIds() != null && !request.getOrderIds().isEmpty()) {
            orders = orderServiceClient.getOrdersBatch(request.getOrderIds());
        } else {
            // Default: Fetch all NEW orders (Ready for assignment)
            com.example.danxils_commons.dto.OrderQueryRequestDto query = com.example.danxils_commons.dto.OrderQueryRequestDto
                    .builder()
                    .statuses(List.of(com.example.danxils_commons.enums.OrderStatus.NEW))
                    .build();
            orders = orderServiceClient.queryOrders(query);
        }

        // 2. Fetch Vehicles & Apply Compliance/Profile Logic
        List<FleetVehicleEntity> rawVehicles;
        if (request.getVehicleIds() != null && !request.getVehicleIds().isEmpty()) {
            rawVehicles = vehicleRepository.findAllById(request.getVehicleIds());
        } else {
            // Default: Use only AVAILABLE vehicles
            rawVehicles = vehicleRepository.findAll().stream()
                    .filter(v -> Boolean.TRUE.equals(v.getAvailable()))
                    .collect(java.util.stream.Collectors.toList());
        }

        List<FleetVehicleEntity> vehicles = rawVehicles.stream()
                // 2a. Carrier Compliance
                .filter(v -> {
                    if (v.getCarrierId() == null)
                        return true;
                    return carrierComplianceRepository.findById(v.getCarrierId())
                            .map(c -> "COMPLIANT".equals(c.getComplianceStatus())
                                    && Boolean.TRUE.equals(c.getIsInsured()))
                            .orElse(false);
                })
                // 2b. Profile Enrichment
                .map(v -> {
                    if (v.getProfileId() != null) {
                        vehicleProfileRepository.findById(v.getProfileId()).ifPresent(p -> {
                            if (p.getMaxCapacityWeight() != null)
                                v.setCapacityWeight(p.getMaxCapacityWeight());
                            if (p.getMaxCapacityVolume() != null)
                                v.setCapacityVolume(p.getMaxCapacityVolume());
                        });
                    }
                    return v;
                })
                .collect(java.util.stream.Collectors.toList());

        log.info("Optimization using {} eligible vehicles (out of {} requested/found)", vehicles.size(),
                rawVehicles.size());

        // 3. Fetch Profile (if provided)
        com.example.planning_service.entity.OptimizationProfileEntity profile = null;
        if (request.getProfileId() != null) {
            profile = com.example.planning_service.service.OptimizationProfileService.class
                    .cast(optimizationProfileService).getProfileEntity(request.getProfileId());
        }

        // 4. Optimize
        VrpOptimizerService.VehicleRoutingSolution solution = vrpOptimizerService.calculateRoutes(orders, vehicles,
                profile);

        // 5. Persist Solution (DRAFT)
        int totalRoutes = solution.routes().size();
        int totalStops = solution.routes().stream().mapToInt(r -> r.activities().size()).sum();
        double totalDist = solution.routes().stream().mapToDouble(r -> r.totalDistance()).sum();
        long totalTime = solution.routes().stream().mapToLong(r -> r.totalTimeMillis() / 1000).sum();

        com.example.planning_service.entity.OptimizationSolutionEntity entity = com.example.planning_service.entity.OptimizationSolutionEntity
                .builder()
                .id(java.util.UUID.randomUUID())
                .createdAt(java.time.Instant.now())
                .status(com.example.planning_service.entity.OptimizationSolutionEntity.Status.DRAFT)
                .solutionJson(solution)
                // Summary
                .name(profile != null
                        ? profile.getName() + " "
                                + java.time.LocalDateTime.now()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        : "Optimization " + java.time.LocalDateTime.now())
                .totalRoutes(totalRoutes)
                .totalStops(totalStops)
                .totalDistanceMeters(totalDist)
                .totalDurationSeconds(totalTime)
                .unassignedOrdersCount(orders.size() - totalStops) // Approx
                .build();
        solutionRepository.save(entity);

        // 6. Trigger Webhooks
        triggerOptimizationWebhooks(solution);

        return ResponseEntity.ok(solution);
    }

    /**
     * NEW ENDPOINT: Full Solution with Timefold Metadata
     * Returns complete VehicleRoutingSolution including:
     * - All vehicles with chain references
     * - All stops with planning variables
     * - Score and statistics
     * - Problem facts (orders)
     * 
     * Used by advanced clients that need full solution reconstruction
     */
    @PostMapping("/solution")
    public ResponseEntity<com.example.planning_service.dto.VehicleRoutingSolutionDto> getFullSolution(
            @RequestBody com.example.planning_service.dto.OptimizationRequestDto request) {
        log.info("Received FULL solution request for {} orders and {} vehicles",
                request.getOrderIds() != null ? request.getOrderIds().size() : 0,
                request.getVehicleIds() != null ? request.getVehicleIds().size() : 0);

        // 1. Trigger optimization (same logic as /optimize)
        // This will populate the cache in TimefoldOptimizer
        optimizeRoutes(request);

        // 2. Retrieve cached solution from optimizer
        if (!(vrpOptimizerService instanceof com.example.planning_service.optimization.impl.TimefoldOptimizer)) {
            log.error("VrpOptimizerService is not TimefoldOptimizer - cannot access solution cache");
            return ResponseEntity.status(500).build();
        }

        com.example.planning_service.optimization.impl.TimefoldOptimizer timefoldOptimizer = (com.example.planning_service.optimization.impl.TimefoldOptimizer) vrpOptimizerService;

        com.example.planning_service.domain.timefold.VehicleRoutingSolution fullSolution = timefoldOptimizer
                .getLastSolution();

        if (fullSolution == null) {
            log.warn("No solution in cache - optimization may have failed");
            return ResponseEntity.notFound().build();
        }

        // 3. Map to DTO using SolutionMapper
        com.example.planning_service.mapper.SolutionMapper solutionMapper = new com.example.planning_service.mapper.SolutionMapper();

        com.example.planning_service.dto.VehicleRoutingSolutionDto solutionDto = solutionMapper.toDto(fullSolution);

        log.info("Returning full solution with {} vehicles, {} stops, score: {}",
                solutionDto.getVehicles() != null ? solutionDto.getVehicles().size() : 0,
                solutionDto.getStops() != null ? solutionDto.getStops().size() : 0,
                solutionDto.getScore());

        return ResponseEntity.ok(solutionDto);
    }

    @GetMapping("/latest")
    public ResponseEntity<VehicleRoutingSolution> getLatestOptimization() {
        log.info("Fetching latest optimization solution");

        // 1. Retrieve latest solution entity from repository
        com.example.planning_service.entity.OptimizationSolutionEntity latestEntity = solutionRepository
                .findTopByOrderByCreatedAtDesc()
                .orElse(null);

        if (latestEntity == null) {
            log.warn("No optimization solution found in database");
            return ResponseEntity.notFound().build();
        }

        // 2. Deserialize solution JSON to VehicleRoutingSolution object
        VehicleRoutingSolution solution;
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            solution = objectMapper.convertValue(
                    latestEntity.getSolutionJson(),
                    VehicleRoutingSolution.class);
        } catch (Exception e) {
            log.error("Failed to deserialize optimization solution JSON", e);
            return ResponseEntity.status(500).build();
        }

        // 3. Enrich solution with driver assignments from order-service
        VehicleRoutingSolution enrichedSolution = driverEnrichmentService.enrichWithDriverAssignments(solution);

        log.info("Returning enriched solution with {} routes", enrichedSolution.routes().size());
        return ResponseEntity.ok(enrichedSolution);
    }

    @PostMapping("/publish")
    public ResponseEntity<Void> publishRoutes(@RequestBody PublishRoutesRequest request) {
        List<com.example.planning_service.entity.DriverTaskEntity> tasks = new ArrayList<>();
        List<UUID> vehicleIds = request.routes().stream()
                .map(r -> UUID.fromString(r.vehicleId()))
                .toList();

        java.util.Map<String, FleetVehicleEntity> vehicleMap = vehicleRepository.findAllById(vehicleIds).stream()
                .collect(java.util.stream.Collectors.toMap(v -> v.getId().toString(), v -> v));

        for (RouteDefinition route : request.routes()) {
            FleetVehicleEntity vehicle = vehicleMap.get(route.vehicleId());
            String driverId = (vehicle != null && vehicle.getDriverId() != null) ? vehicle.getDriverId() : null;

            if (driverId == null) {
                log.warn("Skipping publication for route {} - No driver assigned to vehicle {}", route.vehicleId(),
                        route.vehicleId());
                continue;
            }

            // Log Communication
            com.example.planning_service.entity.CommunicationLogEntity logEntry = com.example.planning_service.entity.CommunicationLogEntity
                    .builder()
                    .id(java.util.UUID.randomUUID())
                    .recipient(driverId)
                    .channel("PUSH_NOTIFICATION") // Simulated
                    .messageContent("New route assigned with " + route.stops().size() + " stops.")
                    .status("SENT")
                    .timestamp(java.time.Instant.now())
                    .metadata(java.util.Map.of("routeId", "generated", "vehicleId", route.vehicleId()))
                    .build();
            communicationLogRepository.save(logEntry);

            // Update Solution Status if ID provided
            if (request.solutionId() != null) {
                solutionRepository.findById(request.solutionId()).ifPresent(solution -> {
                    solution.setStatus(com.example.planning_service.entity.OptimizationSolutionEntity.Status.PUBLISHED);
                    solutionRepository.save(solution);
                });
            }

            for (RouteStop stop : route.stops()) {
                com.example.planning_service.entity.DriverTaskEntity task = com.example.planning_service.entity.DriverTaskEntity
                        .builder()
                        .driverId(driverId)
                        .customerName("Customer " + stop.orderId()) // Ideally fetch details
                        .address(stop.address())
                        .status("PENDING")
                        .orderId(stop.orderId())
                        .assignedAt(java.time.LocalDateTime.now())
                        .build();
                tasks.add(task);

                // Propagate to Order Service
                try {
                    orderServiceClient.assignDriver(
                            java.util.UUID.fromString(stop.orderId()),
                            new com.example.planning_service.dto.request.AssignDriverRequestDto(driverId));
                } catch (Exception e) {
                    log.error("Failed to propagate driver assignment for order {} to Order Service", stop.orderId(), e);
                }
            }

            // Create Modern Manifest
            try {
                List<com.example.planning_service.entity.ManifestRouteEntity> manifestRoutes = route.stops().stream()
                        .map(stop -> com.example.planning_service.entity.ManifestRouteEntity.builder()
                                .orderId(java.util.UUID.fromString(stop.orderId()))
                                .address(stop.address())
                                .sequence(route.stops().indexOf(stop) + 1)
                                .estimatedArrival("00:00") // TODO: Pass from frontend
                                .timeWindow("09:00-17:00")
                                .build())
                        .collect(java.util.stream.Collectors.toList());

                com.example.planning_service.entity.ManifestEntity createdManifest = manifestService.createManifest(
                        java.util.UUID.fromString(driverId),
                        java.util.UUID.fromString(vehicle.getId().toString()),
                        java.time.LocalDate.now(),
                        manifestRoutes);
                log.info("Generated Modern Manifest {} for Driver {}", createdManifest.getExternalReference(),
                        driverId);
            } catch (Exception e) {
                log.error("Failed to generate manifest", e);
            }
        }
        driverTaskRepository.saveAll(tasks);
        log.info("Published {} tasks to assigned drivers", tasks.size());

        return ResponseEntity.ok().build();

    }

    /**
     * Get specific optimization solution by ID
     * Used by dispatcher to review optimization details
     */
    @org.springframework.web.bind.annotation.GetMapping("/routes/{routeId}")
    public ResponseEntity<VrpOptimizerService.VehicleRoutingSolution> getRouteById(
            @org.springframework.web.bind.annotation.PathVariable UUID routeId) {
        return solutionRepository.findById(routeId)
                .map(entity -> {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        String json = mapper.writeValueAsString(entity.getSolutionJson());
                        VrpOptimizerService.VehicleRoutingSolution sol = mapper.readValue(json,
                                VrpOptimizerService.VehicleRoutingSolution.class);
                        return ResponseEntity.ok(sol);
                    } catch (Exception e) {
                        log.error("Failed to deserialize solution for route {}", routeId, e);
                        return ResponseEntity.internalServerError().<VrpOptimizerService.VehicleRoutingSolution>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all tasks assigned to a specific driver
     * Used by Driver PWA to display route sequence
     */
    @org.springframework.web.bind.annotation.GetMapping("/routes/driver/{driverId}")
    public ResponseEntity<List<com.example.planning_service.entity.DriverTaskEntity>> getDriverRoute(
            @org.springframework.web.bind.annotation.PathVariable String driverId) {
        List<com.example.planning_service.entity.DriverTaskEntity> tasks = driverTaskRepository
                .findByDriverIdAndStatusNot(driverId, "COMPLETED");

        return ResponseEntity.ok(tasks);
    }

    public record PublishRoutesRequest(java.util.UUID solutionId, List<RouteDefinition> routes) {
    }

    public record RouteDefinition(String vehicleId, List<RouteStop> stops) {
    }

    public record RouteStop(String orderId, String address) {
    }

    private void triggerOptimizationWebhooks(VrpOptimizerService.VehicleRoutingSolution results) {
        // ... (existing logic)
    }
}
