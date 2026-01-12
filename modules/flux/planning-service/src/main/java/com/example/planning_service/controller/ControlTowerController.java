package com.example.planning_service.controller;

import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.planning_service.entity.PlannedRouteEntity;
import com.example.planning_service.service.RouteManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/control-tower")
@RequiredArgsConstructor
public class ControlTowerController {

    private final RouteManagementService routeService;

    /**
     * Trigger: Start the day by loading standard routes.
     */
    @PostMapping("/instantiate")
    public ResponseEntity<List<PlannedRouteEntity>> instantiateStandardRoutes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(routeService.instantiateStandardRoutes(date));
    }

    /**
     * Ad-hoc: Inject an order into a route and re-optimize locally.
     */
    @PostMapping("/routes/{routeId}/inject")
    public ResponseEntity<PlannedRouteEntity> injectOrder(
            @PathVariable UUID routeId,
            @RequestBody OrderResponseDto order) {
        return ResponseEntity.ok(routeService.injectOrder(routeId, order));
    }
}
