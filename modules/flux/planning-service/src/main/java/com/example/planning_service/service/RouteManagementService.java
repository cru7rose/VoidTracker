package com.example.planning_service.service;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.planning_service.dto.RouteStopDto;
import com.example.planning_service.entity.MilkrunTemplateEntity;
import com.example.planning_service.entity.PlannedRouteEntity;
import com.example.planning_service.optimization.impl.TimefoldOptimizer;
import com.example.planning_service.repository.MilkrunTemplateRepository;
import com.example.planning_service.repository.PlannedRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RouteManagementService {

    private final MilkrunTemplateRepository templateRepository;
    private final PlannedRouteRepository routeRepository;
    private final TimefoldOptimizer optimizer; // For re-sequencing

    /**
     * Instantiates Standard Routes (Milkruns) for a given day.
     * This is the "Start Day" trigger for the Control Tower.
     */
    @Transactional
    public List<PlannedRouteEntity> instantiateStandardRoutes(LocalDate date) {
        // 1. Fetch active templates
        List<MilkrunTemplateEntity> templates = templateRepository.findAll().stream()
                .filter(MilkrunTemplateEntity::isActive)
                // TODO: Add Schedule/Cron check here
                .collect(Collectors.toList());

        List<PlannedRouteEntity> createdRoutes = new ArrayList<>();

        for (MilkrunTemplateEntity template : templates) {
            PlannedRouteEntity route = PlannedRouteEntity.builder()
                    .planningDate(date)
                    .templateId(template.getId())
                    .name(template.getName() + " - " + date.toString())
                    .vehicleId(template.getVehicleId())
                    .driverId(template.getDriverId())
                    .locked(false) // Initially unlocked? Or locked because it's standard? Let's say unlocked until modified.
                    .stops(template.getStops().stream()
                            .map(stop -> PlannedRouteEntity.PlannedStop.builder()
                                    .lat(stop.getLat())
                                    .lon(stop.getLon())
                                    .type(stop.getActivityType())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            createdRoutes.add(routeRepository.save(route));
        }
        
        log.info("Instantiated {} standard routes for {}", createdRoutes.size(), date);
        return createdRoutes;
    }

    /**
     * Ad-hoc Injection: Adds an order to an existing route and re-sequences it.
     * Locks the route to prevent global optimization overrides.
     */
    @Transactional
    public PlannedRouteEntity injectOrder(UUID routeId, OrderResponseDto order) {
        PlannedRouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found: " + routeId));

        // 1. Add new stop
        PlannedRouteEntity.PlannedStop newStop = PlannedRouteEntity.PlannedStop.builder()
                .orderId(order.orderId())
                .type("DELIVERY") // Simplified
                .lat(order.delivery().lat())
                .lon(order.delivery().lon())
                .manual(true)
                .build();
        
        route.getStops().add(newStop);

        // 2. Resequence (Local Optimization)
        // Convert to DTO for optimizer
        List<RouteStopDto> stopDtos = route.getStops().stream()
                .map(s -> RouteStopDto.builder()
                        .lat(s.getLat())
                        .lon(s.getLon())
                        .orderId(s.getOrderId())
                        .type(s.getType())
                        .build())
                .collect(Collectors.toList());

        // Assuming start from Depot (first stop or 0,0) - improvement needed
        double startLat = stopDtos.isEmpty() ? 0 : stopDtos.get(0).getLat();
        double startLon = stopDtos.isEmpty() ? 0 : stopDtos.get(0).getLon();

        List<RouteStopDto> optimizedDtos = optimizer.resequenceRoutes(stopDtos, startLat, startLon);

        // 3. Map back and Save
        List<PlannedRouteEntity.PlannedStop> optimizedStops = optimizedDtos.stream()
                .map(dto -> PlannedRouteEntity.PlannedStop.builder()
                        .orderId(dto.getOrderId())
                        .type(dto.getType())
                        .lat(dto.getLat())
                        .lon(dto.getLon())
                        .manual(true) // Preserve manual flag? Need better mapping logic
                        .build())
                .collect(Collectors.toList());
        
        route.setStops(optimizedStops);
        route.setLocked(true); // LOCK THE ROUTE

        return routeRepository.save(route);
    }
}
