package com.example.dashboard_service.controller;

import com.example.dashboard_service.dto.HeatmapPointDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/dashboard/lenses")
@RequiredArgsConstructor
public class LensesController {

    private final com.example.dashboard_service.client.OrderServiceClient orderClient;
    private final com.example.dashboard_service.client.AnalyticsServiceClient analyticsClient;

    @GetMapping("/profitability")
    public ResponseEntity<List<HeatmapPointDto>> getProfitabilityHeatmap() {
        // Real Data: Fetch orders (Delivered = POD status)
        // For MVP: We visualize "Volume of Business" as proxy for profitability
        var ordersPage = orderClient.getOrders(
                List.of(com.example.danxils_commons.enums.OrderStatus.POD),
                null, null, 0, 1000, "created,desc");

        List<HeatmapPointDto> points = ordersPage.getContent().stream()
                .filter(o -> o.delivery() != null && o.delivery().lat() != null && o.delivery().lon() != null)
                .map(o -> new HeatmapPointDto(
                        o.delivery().lat(),
                        o.delivery().lon(),
                        0.8 // High intensity for completed business
                ))
                .toList();

        return ResponseEntity.ok(points);
    }

    @GetMapping("/risk")
    public ResponseEntity<List<HeatmapPointDto>> getRiskHeatmap() {
        // Real Data: "Risk" = Orders that are late (SLA violated) or missing
        // coordinates
        // We fetch active orders (NEW, PICKUP, PSIP, LOAD)
        var ordersPage = orderClient.getOrders(
                List.of(com.example.danxils_commons.enums.OrderStatus.NEW,
                        com.example.danxils_commons.enums.OrderStatus.PICKUP,
                        com.example.danxils_commons.enums.OrderStatus.PSIP,
                        com.example.danxils_commons.enums.OrderStatus.LOAD),
                null, null, 0, 1000, "created,desc");

        List<HeatmapPointDto> points = ordersPage.getContent().stream()
                .filter(o -> o.delivery() != null && o.delivery().lat() != null && o.delivery().lon() != null)
                .map(o -> {
                    // Risk Calculation: If SLA is passed, Intensity = 1.0 (Red)
                    // If SLA is close (< 2h), Intensity = 0.5 (Yellow)
                    // Else Intensity = 0.2 (Green/Low Risk)
                    double intensity = 0.2;
                    if (o.delivery().sla() != null) {
                        if (java.time.Instant.now().isAfter(o.delivery().sla())) {
                            intensity = 1.0;
                        } else if (java.time.Instant.now().plusSeconds(7200).isAfter(o.delivery().sla())) {
                            intensity = 0.6;
                        }
                    }
                    return new HeatmapPointDto(o.delivery().lat(), o.delivery().lon(), intensity);
                })
                .toList();

        return ResponseEntity.ok(points);
    }
}
