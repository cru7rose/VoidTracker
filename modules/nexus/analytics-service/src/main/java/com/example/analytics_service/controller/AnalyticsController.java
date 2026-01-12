// File: analytics-service/src/main/java/com/example/analytics_service/controller/AnalyticsController.java
package com.example.analytics_service.controller;

import com.example.analytics_service.dto.KpiDto;
// ✅ FIX: Zaktualizowano importy do nowej lokalizacji w danxils-commons
import com.example.danxils_commons.dto.LatestLocationDto;
import com.example.danxils_commons.dto.LocationHistoryDto;
import com.example.analytics_service.repository.AnalyticsOrderRepository;
import com.example.analytics_service.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for retrieving business intelligence and KPIs.")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsOrderRepository analyticsRepository;
    private final AnalyticsService analyticsService;

    @GetMapping("/kpi/total-orders")
    @Operation(summary = "Get the total number of processed orders")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the KPI.")
    public ResponseEntity<KpiDto> getTotalOrdersKpi() {
        long totalOrders = analyticsRepository.count();
        KpiDto kpi = new KpiDto(
                "total_orders",
                totalOrders,
                "Całkowita liczba zleceń przetworzonych przez system."
        );
        return ResponseEntity.ok(kpi);
    }

    @GetMapping("/orders/{orderId}/track")
    @Operation(summary = "Get the full location history for a specific order")
    @ApiResponse(responseCode = "200", description = "Location history retrieved successfully.")
    @ApiResponse(responseCode = "404", description = "Order not found.")
    public ResponseEntity<List<LocationHistoryDto>> getOrderTrack(@PathVariable UUID orderId) {
        List<LocationHistoryDto> track = analyticsService.getOrderLocationHistory(orderId);
        if (track.isEmpty()) {
            return ResponseEntity.ok(track);
        }
        return ResponseEntity.ok(track);
    }

    @PostMapping("/locations/latest")
    @Operation(summary = "Get the latest known locations for a list of order IDs")
    public ResponseEntity<List<LatestLocationDto>> getLatestLocations(@RequestBody List<UUID> orderIds) {
        return ResponseEntity.ok(analyticsService.getLatestLocationsForOrders(orderIds));
    }
}