package com.example.admin_panel_service.controller;

import com.example.danxils_commons.dto.RoutePlanDto;
import com.example.admin_panel_service.service.RoutePlanAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kontroler REST do zarządzania szablonami planów tras, oczyszczony
 * z ręcznego zarządzania tokenami. Polega na automatycznej propagacji kontekstu
 * bezpieczeństwa przez globalny interceptor Feign.
 */
@RestController
@RequestMapping("/api/admin/route-plans")
@RequiredArgsConstructor
public class RoutePlanAdminController {

    private final RoutePlanAdminService routePlanAdminService;

    @GetMapping
    public ResponseEntity<List<RoutePlanDto>> getAllRoutePlans() {
        return ResponseEntity.ok(routePlanAdminService.getAllRoutePlans());
    }

    @PostMapping
    public ResponseEntity<RoutePlanDto> createRoutePlan(@RequestBody RoutePlanDto routePlanDto) {
        RoutePlanDto createdPlan = routePlanAdminService.createRoutePlan(routePlanDto);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<RoutePlanDto> getRoutePlanById(@PathVariable UUID planId) {
        return ResponseEntity.ok(routePlanAdminService.getRoutePlanById(planId));
    }

    @PutMapping("/{planId}")
    public ResponseEntity<RoutePlanDto> updateRoutePlan(@PathVariable UUID planId, @RequestBody RoutePlanDto routePlanDto) {
        RoutePlanDto updatedPlan = routePlanAdminService.updateRoutePlan(planId, routePlanDto);
        return ResponseEntity.ok(updatedPlan);
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deleteRoutePlan(@PathVariable UUID planId) {
        routePlanAdminService.deleteRoutePlan(planId);
        return ResponseEntity.noContent().build();
    }
}