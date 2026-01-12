package com.example.admin_panel_service.client;

import com.example.admin_panel_service.client.fallback.PlanningServiceClientFallback;
import com.example.danxils_commons.dto.FleetVehicleDto;
import com.example.danxils_commons.dto.RoutePlanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Klient Feign do `planning-service`. Został oczyszczony z ręcznego
 * przekazywania nagłówka 'Authorization'. Propagacja tokenu jest zarządzana globalnie.
 */
@FeignClient(name = "planning-service", fallback = PlanningServiceClientFallback.class)
public interface PlanningServiceClient {

    // --- Route Plan Management ---
    @GetMapping("/api/planning/plans")
    List<RoutePlanDto> getAllRoutePlans();

    @PostMapping("/api/planning/plans")
    RoutePlanDto createRoutePlan(@RequestBody RoutePlanDto routePlanDto);

    @GetMapping("/api/planning/plans/{planId}")
    RoutePlanDto getRoutePlanById(@PathVariable("planId") UUID planId);

    @PutMapping("/api/planning/plans/{planId}")
    RoutePlanDto updateRoutePlan(@PathVariable("planId") UUID planId, @RequestBody RoutePlanDto routePlanDto);

    @DeleteMapping("/api/planning/plans/{planId}")
    void deleteRoutePlan(@PathVariable("planId") UUID planId);

    // --- Fleet Management ---
    @GetMapping("/api/admin/fleet/vehicles")
    List<FleetVehicleDto> getAllVehicles();

    @PostMapping("/api/admin/fleet/vehicles")
    FleetVehicleDto createVehicle(@RequestBody FleetVehicleDto vehicleDto);

    @GetMapping("/api/admin/fleet/vehicles/{vehicleId}")
    FleetVehicleDto getVehicleById(@PathVariable("vehicleId") UUID vehicleId);

    @PutMapping("/api/admin/fleet/vehicles/{vehicleId}")
    FleetVehicleDto updateVehicle(@PathVariable("vehicleId") UUID vehicleId, @RequestBody FleetVehicleDto vehicleDto);

    @DeleteMapping("/api/admin/fleet/vehicles/{vehicleId}")
    void deleteVehicle(@PathVariable("vehicleId") UUID vehicleId);
}