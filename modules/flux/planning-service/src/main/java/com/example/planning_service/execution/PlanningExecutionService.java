// File: planning-service/src/main/java/com/example/planning_service/execution/PlanningExecutionService.java
package com.example.planning_service.execution;

import com.example.danxils_commons.dto.OrderResponseDto;
// ✅ FIX: Zaktualizowano import do nowej lokalizacji w danxils-commons
import com.example.danxils_commons.dto.OrderQueryRequestDto;
import com.example.danxils_commons.event.RoutePlannedEvent;
import com.example.planning_service.client.OrderServiceClient;
import com.example.planning_service.config.AppProperties;
import com.example.planning_service.entity.FleetVehicleEntity;
import com.example.planning_service.entity.ManifestEntity;
import com.example.planning_service.entity.OptimizationProfileEntity;
import com.example.planning_service.entity.RoutePlanEntity;
import com.example.planning_service.optimization.VrpOptimizerService;
import com.example.planning_service.repository.CarrierComplianceRepository;
import com.example.planning_service.repository.FleetVehicleRepository;
import com.example.planning_service.repository.OptimizationProfileRepository;
import com.example.planning_service.repository.RoutePlanRepository;
import com.example.planning_service.repository.VehicleProfileRepository;
import com.example.planning_service.service.ManifestService;
import com.example.planning_service.monitoring.OptimizationMetrics;
import com.example.planning_service.monitoring.SlaAlertService;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlanningExecutionService {

    private final RoutePlanRepository planRepository;
    private final FleetVehicleRepository vehicleRepository;
    private final OptimizationProfileRepository optimizationProfileRepository;
    private final OrderServiceClient orderServiceClient;
    private final VrpOptimizerService vrpOptimizer;
    private final ManifestService manifestService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppProperties appProperties;
    private final VehicleProfileRepository vehicleProfileRepository;
    private final CarrierComplianceRepository carrierComplianceRepository;
    private final OptimizationMetrics metrics;
    private final SlaAlertService slaAlertService;

    @Transactional
    public void executePlan(UUID planId) {
        log.info("Executing route plan with ID: {}", planId);
        RoutePlanEntity plan = planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("RoutePlanEntity not found with ID: " + planId));

        List<FleetVehicleEntity> availableVehicles = getEligibleFleet();

        if (availableVehicles.isEmpty()) {
            log.warn("No vehicles available in the fleet (after compliance check). Cannot execute plan {}.", planId);
            return;
        }

        OrderQueryRequestDto query = new OrderQueryRequestDto(
                List.of(com.example.danxils_commons.enums.OrderStatus.NEW),
                plan.getFilters().customerIds(),
                plan.getFilters().postalCodePrefixes(),
                plan.getFilters().priorities());
        List<OrderResponseDto> ordersToPlan = orderServiceClient.queryOrders(query);

        if (ordersToPlan.isEmpty()) {
            log.info("No orders found matching criteria for plan '{}'. Execution skipped.", plan.getName());
            return;
        }
        log.info("Found {} orders to be planned for plan '{}'.", ordersToPlan.size(), plan.getName());
        VrpOptimizerService.VehicleRoutingSolution solution = vrpOptimizer.calculateRoutes(ordersToPlan, availableVehicles, null);

        // Create manifests from VRP solution routes
        solution.routes().forEach(route -> {
            // Create manifest entity from route
            ManifestEntity manifest = manifestService.createManifestFromVehicleRoutingSolution(route, java.time.LocalDate.now());
            log.info("Created manifest {} for vehicle {}", manifest.getId(), route.vehicleId());

            // Publish route planned event
            RoutePlannedEvent event = createEventFromRoute(planId, route);
            publishEvent(appProperties.getKafka().getTopics().getRoutePlanned(), planId.toString(), event);
        });
    }

    /**
     * NOWA METODA: Execute planning for a specific batch of order IDs
     * 
     * Używane przez BatchOptimizationScheduler dla auto-planning flow.
     * W przeciwieństwie do executePlan(), ta metoda:
     * - Nie wymaga RoutePlanEntity (batch nie ma "planu", tylko listę zamówień)
     * - Fetch orders bezpośrednio po IDs (nie przez filtry)
     * - Używa przekazanego profileId dla optimization constraints
     * 
     * @param orderIds  Lista UUID zamówień do optymalizacji
     * @param profileId UUID profilu optymalizacyjnego (może być null dla default)
     */
    @Transactional
    public void executePlanForOrderIds(List<UUID> orderIds, UUID profileId) {
        log.info("Executing batch planning for {} order IDs with profile {}",
                orderIds.size(), profileId);

        // 1. Validate inputs
        if (orderIds == null || orderIds.isEmpty()) {
            log.warn("Empty order IDs list, nothing to plan");
            return;
        }

        // 2. Fetch available vehicles
        List<FleetVehicleEntity> availableVehicles = getEligibleFleet();

        if (availableVehicles.isEmpty()) {
            log.warn("No available vehicles in fleet (after compliance check). Cannot execute batch planning.");
            return;
        }
        log.info("Found {} available vehicles for batch planning", availableVehicles.size());

        // 3. Fetch orders from order-service by IDs
        List<OrderResponseDto> ordersToPlan;
        try {
            // Używamy istniejącego Feign client do pobrania zamówień
            // Zakładam że OrderServiceClient ma metodę getOrdersByIds() lub podobną
            // Jeśli nie, trzeba dodać

            // Temporary workaround - pobieramy wszystkie NEW orders i filtrujemy
            OrderQueryRequestDto query = new OrderQueryRequestDto(
                    List.of(com.example.danxils_commons.enums.OrderStatus.NEW),
                    null, // customerIds
                    null, // postalCodePrefixes
                    null // priorities
            );
            List<OrderResponseDto> allNewOrders = orderServiceClient.queryOrders(query);

            // Filter only requested IDs
            ordersToPlan = allNewOrders.stream()
                    .filter(order -> orderIds.contains(order.orderId()))
                    .collect(Collectors.toList());

            log.info("Fetched {} orders (out of {} requested IDs)",
                    ordersToPlan.size(), orderIds.size());

            if (ordersToPlan.isEmpty()) {
                log.warn("No orders found for batch IDs, possibly already processed or invalid IDs");
                return;
            }

        } catch (Exception e) {
            log.error("Failed to fetch orders from order-service: {}", e.getMessage(), e);
            throw new RuntimeException("Order fetch failed in batch planning", e);
        }

        // 4. Get optimization profile
        OptimizationProfileEntity profile = null;
        if (profileId != null) {
            try {
                profile = optimizationProfileRepository.findById(profileId)
                        .orElse(null);
                if (profile != null) {
                    log.info("Using optimization profile: {} ({})", profile.getName(), profileId);
                } else {
                    log.warn("Profile {} not found, using default constraints", profileId);
                }
            } catch (Exception e) {
                log.warn("Error fetching profile {}: {}", profileId, e.getMessage());
            }
        }

        // 5. Run VRP optimization with metrics tracking
        log.info("🚀 Starting Timefold solver for {} orders and {} vehicles",
                ordersToPlan.size(), availableVehicles.size());

        Timer.Sample timerSample = metrics.startOptimizationTimer();
        VrpOptimizerService.VehicleRoutingSolution solution;
        try {
            solution = vrpOptimizer.calculateRoutes(ordersToPlan, availableVehicles, profile);

            // Stop timer and record success
            timerSample.stop(metrics.getOptimizationDurationTimer());
            metrics.recordOptimizationSuccess();
        } catch (Exception e) {
            log.error("❌ VRP optimization FAILED: {}", e.getMessage(), e);
            timerSample.stop(metrics.getOptimizationDurationTimer());
            metrics.recordOptimizationFailure();
            throw new RuntimeException("Batch optimization failed", e);
        }

        int routeCount = solution.routes().size();
        log.info("✅ Optimization completed: {} routes generated", routeCount);

        // 6. Create manifests from solution
        solution.routes().forEach(route -> {
            try {
                ManifestEntity manifest = manifestService.createManifestFromVehicleRoutingSolution(
                        route, java.time.LocalDate.now());
                log.info("Created manifest {} for vehicle {} ({} stops)",
                        manifest.getId(), route.vehicleId(), route.activities().size());

                // 7. Publish route planned event
                RoutePlannedEvent event = createEventFromRoute(null, route); // planId = null for batch
                publishEvent(appProperties.getKafka().getTopics().getRoutePlanned(),
                        manifest.getId().toString(), event);

            } catch (Exception e) {
                log.error("Failed to create manifest for vehicle {}: {}",
                        route.vehicleId(), e.getMessage(), e);
            }
        });

        log.info("🌙 Batch planning completed: {} orders processed, {} routes created",
                ordersToPlan.size(), routeCount);

        // Record metrics
        metrics.recordRoutesCreated(routeCount);
        metrics.updateUnplannedOrdersCount(0); // Batch processed

        // SLA compliance check
        slaAlertService.checkSlaAfterOptimization(routeCount, ordersToPlan.size());
    }

    private List<FleetVehicleEntity> getEligibleFleet() {
        return vehicleRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getAvailable()))
                // 1. CARRIER COMPLIANCE CHECK
                .filter(v -> {
                    if (v.getCarrierId() == null)
                        return true; // Internal fleet is always compliant
                    return carrierComplianceRepository.findById(v.getCarrierId())
                            .map(c -> "COMPLIANT".equals(c.getComplianceStatus())
                                    && Boolean.TRUE.equals(c.getIsInsured()))
                            .orElse(false); // Unknown carrier = Not compliant
                })
                // 2. PROFILE ENRICHMENT
                .map(v -> {
                    if (v.getProfileId() != null) {
                        vehicleProfileRepository.findById(v.getProfileId()).ifPresent(p -> {
                            // Override capacity from profile if set
                            if (p.getMaxCapacityWeight() != null)
                                v.setCapacityWeight(p.getMaxCapacityWeight());
                            if (p.getMaxCapacityVolume() != null)
                                v.setCapacityVolume(p.getMaxCapacityVolume());
                        });
                    }
                    return v;
                })
                .collect(Collectors.toList());
    }

    private RoutePlannedEvent createEventFromRoute(UUID planId, VrpOptimizerService.VehicleRoutingSolution.Route route) {
        List<UUID> includedOrderIds = route.activities().stream()
                .map(VrpOptimizerService.VehicleRoutingSolution.Activity::orderId)
                .distinct()
                .collect(Collectors.toList());
        List<RoutePlannedEvent.Waypoint> waypoints = route.activities().stream()
                .map(activity -> RoutePlannedEvent.Waypoint.builder()
                        .orderId(activity.orderId())
                        .type(RoutePlannedEvent.WaypointType.valueOf(activity.type().name()))
                        .latitude(activity.latitude())
                        .longitude(activity.longitude())
                        .sequence(route.activities().indexOf(activity) + 1)
                        .estimatedArrivalTimeMillis(activity.arrivalTimeMillis())
                        .build())
                .collect(Collectors.toList());

        return RoutePlannedEvent.builder()
                .planId(planId)
                .createdAt(Instant.now())
                .vehicleId(route.vehicleId())
                .includedOrderIds(includedOrderIds)
                .waypoints(waypoints)
                .totalDistanceMeters(route.totalDistance())
                .totalTimeMillis(route.totalTimeMillis())
                .build();
    }

    private void publishEvent(String topic, String key, Object event) {
        try {
            log.info("Publishing event {} to topic '{}' with key {}", event.getClass().getSimpleName(), topic, key);
            kafkaTemplate.send(topic, key, event);
        } catch (Exception e) {
            log.error("CRITICAL ERROR: Failed to publish planning event for key {}.", key, e);
        }
    }
}