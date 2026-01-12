package com.example.dashboard_service.controller;

import com.example.dashboard_service.dto.DashboardDtos.*;
import com.example.dashboard_service.dto.DashboardOrderItemDto;
import com.example.dashboard_service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.dashboard_service.dto.AssignDriverRequestDto;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kontroler REST dla dashboardu, oczyszczony z ręcznego
 * zarządzania tokenami. Przekazywanie nagłówka 'Authorization' do wywołań
 * Feign jest teraz zautomatyzowane przez globalny interceptor.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @GetMapping("/orders/active")
    public ResponseEntity<List<DashboardOrderItemDto>> getActiveOrders(
            @RequestParam(required = false) String driverId,
            Pageable pageable) {
        return ResponseEntity.ok(dashboardService.getActiveOrders(driverId, pageable));
    }

    @GetMapping("/map/drivers")
    public ResponseEntity<List<ActiveDriverLocationDto>> getActiveDriversLocation() {
        return ResponseEntity.ok(dashboardService.getActiveDriversLocation());
    }

    @PostMapping("/orders/{orderId}/assign-driver")
    public ResponseEntity<OrderResponseDto> assignDriver(
            @PathVariable UUID orderId,
            @RequestBody AssignDriverRequestDto request) {
        OrderResponseDto updatedOrder = dashboardService.assignDriverToOrder(orderId, request);
        return ResponseEntity.ok(updatedOrder);
    }
}