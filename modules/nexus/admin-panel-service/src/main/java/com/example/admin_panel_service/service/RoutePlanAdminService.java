package com.example.admin_panel_service.service;

import com.example.admin_panel_service.client.PlanningServiceClient;
import com.example.danxils_commons.dto.RoutePlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Warstwa serwisowa do zarządzania planami tras, oczyszczona
 * z ręcznego zarządzania tokenami.
 */
@Service
@RequiredArgsConstructor
public class RoutePlanAdminService {

    private final PlanningServiceClient planningServiceClient;

    public List<RoutePlanDto> getAllRoutePlans() {
        return planningServiceClient.getAllRoutePlans();
    }

    public RoutePlanDto createRoutePlan(RoutePlanDto routePlanDto) {
        return planningServiceClient.createRoutePlan(routePlanDto);
    }

    public RoutePlanDto getRoutePlanById(UUID planId) {
        return planningServiceClient.getRoutePlanById(planId);
    }

    public RoutePlanDto updateRoutePlan(UUID planId, RoutePlanDto routePlanDto) {
        return planningServiceClient.updateRoutePlan(planId, routePlanDto);
    }

    public void deleteRoutePlan(UUID planId) {
        planningServiceClient.deleteRoutePlan(planId);
    }
}