// File: planning-service/src/main/java/com/example/planning_service/controller/RoutePlanController.java
package com.example.planning_service.controller;

import com.example.planning_service.dto.RoutePlanDto;
import com.example.planning_service.execution.PlanningExecutionService;
import com.example.planning_service.service.RoutePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kompletna implementacja kontrolera REST do zarządzania szablonami
 * planów tras (RoutePlan). Udostępnia pełen zestaw endpointów CRUD oraz endpoint
 * do ręcznego uruchamiania planowania, delegując logikę do odpowiednich serwisów.
 */
@RestController
@RequestMapping("/api/planning/plans")
@RequiredArgsConstructor
@Tag(name = "Route Plan Management", description = "API for managing and executing route plans.")
public class RoutePlanController {

    private final PlanningExecutionService executionService;
    private final RoutePlanService routePlanService;

    @PostMapping
    @Operation(summary = "Create a new route plan template")
    public ResponseEntity<RoutePlanDto> createRoutePlan(@Valid @RequestBody RoutePlanDto routePlanDto) {
        RoutePlanDto createdPlan = routePlanService.createPlan(routePlanDto);
        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get a route plan template by ID")
    public ResponseEntity<RoutePlanDto> getRoutePlan(@PathVariable UUID planId) {
        return ResponseEntity.ok(routePlanService.getPlanById(planId));
    }

    @GetMapping
    @Operation(summary = "Get all route plan templates")
    public ResponseEntity<List<RoutePlanDto>> getAllRoutePlans() {
        return ResponseEntity.ok(routePlanService.getAllPlans());
    }

    @PutMapping("/{planId}")
    @Operation(summary = "Update an existing route plan template")
    public ResponseEntity<RoutePlanDto> updateRoutePlan(@PathVariable UUID planId, @Valid @RequestBody RoutePlanDto routePlanDto) {
        return ResponseEntity.ok(routePlanService.updatePlan(planId, routePlanDto));
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "Delete a route plan template")
    public ResponseEntity<Void> deleteRoutePlan(@PathVariable UUID planId) {
        routePlanService.deletePlan(planId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{planId}/execute")
    @Operation(summary = "Manually trigger the execution of a route plan")
    public ResponseEntity<Void> executeRoutePlan(@PathVariable UUID planId) {
        executionService.executePlan(planId);
        return ResponseEntity.accepted().build();
    }
}