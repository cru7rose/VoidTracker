package com.example.admin_panel_service.client.fallback;

import com.example.admin_panel_service.client.PlanningServiceClient;
import com.example.danxils_commons.dto.FleetVehicleDto;
import com.example.danxils_commons.dto.RoutePlanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Implementacja logiki zastępczej (fallback) dla PlanningServiceClient.
 * Sygnatury metod zostały zaktualizowane, aby były zgodne z oczyszczonym
 * interfejsem klienta Feign.
 */
@Slf4j
@Component
public class PlanningServiceClientFallback implements PlanningServiceClient {

    // --- Route Plan Fallbacks ---
    @Override
    public List<RoutePlanDto> getAllRoutePlans() {
        log.error("CIRCUIT BREAKER: Fallback for getAllRoutePlans triggered. Planning-service is unavailable.");
        return Collections.emptyList();
    }

    @Override
    public RoutePlanDto createRoutePlan(RoutePlanDto routePlanDto) {
        log.error("CIRCUIT BREAKER: Fallback for createRoutePlan triggered. Planning-service is unavailable.");
        throw new IllegalStateException("Planning service is currently unavailable. Please try again later.");
    }

    @Override
    public RoutePlanDto getRoutePlanById(UUID planId) {
        log.error("CIRCUIT BREAKER: Fallback for getRoutePlanById triggered for plan {}. Planning-service is unavailable.", planId);
        return null;
    }

    @Override
    public RoutePlanDto updateRoutePlan(UUID planId, RoutePlanDto routePlanDto) {
        log.error("CIRCUIT BREAKER: Fallback for updateRoutePlan triggered for plan {}. Planning-service is unavailable.", planId);
        throw new IllegalStateException("Planning service is currently unavailable. Please try again later.");
    }

    @Override
    public void deleteRoutePlan(UUID planId) {
        log.error("CIRCUIT BREAKER: Fallback for deleteRoutePlan triggered for plan {}. Planning-service is unavailable.", planId);
        throw new IllegalStateException("Planning service is currently unavailable. Please try again later.");
    }

    // --- Fleet Management Fallbacks ---
    @Override
    public List<FleetVehicleDto> getAllVehicles() {
        log.error("CIRCUIT BREAKER: Fallback for getAllVehicles triggered. Planning-service is unavailable.");
        return Collections.emptyList();
    }

    @Override
    public FleetVehicleDto createVehicle(FleetVehicleDto vehicleDto) {
        log.error("CIRCUIT BREAKER: Fallback for createVehicle triggered. Planning-service is unavailable.");
        throw new IllegalStateException("Planning service is currently unavailable. Please try again later.");
    }

    @Override
    public FleetVehicleDto getVehicleById(UUID vehicleId) {
        log.error("CIRCUIT BREAKER: Fallback for getVehicleById triggered for vehicle {}. Planning-service is unavailable.", vehicleId);
        return null;
    }

    @Override
    public FleetVehicleDto updateVehicle(UUID vehicleId, FleetVehicleDto vehicleDto) {
        log.error("CIRCUIT BREAKER: Fallback for updateVehicle triggered for vehicle {}. Planning-service is unavailable.", vehicleId);
        throw new IllegalStateException("Planning service is currently unavailable. Please try again later.");
    }

    @Override
    public void deleteVehicle(UUID vehicleId) {
        log.error("CIRCUIT BREAKER: Fallback for deleteVehicle triggered for vehicle {}. Planning-service is unavailable.", vehicleId);
        throw new IllegalStateException("Planning service is currently unavailable. Please try again later.");
    }
}