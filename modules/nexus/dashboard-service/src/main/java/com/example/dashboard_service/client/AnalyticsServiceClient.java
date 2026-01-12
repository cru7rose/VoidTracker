package com.example.dashboard_service.client;

import com.example.danxils_commons.dto.LatestLocationDto;
import com.example.danxils_commons.dto.LocationHistoryDto;
import com.example.dashboard_service.client.fallback.AnalyticsServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Klient Feign do `analytics-service`, oczyszczony z ręcznego
 * przekazywania nagłówka autoryzacyjnego. Propagacja tokenu jest teraz zarządzana globalnie.
 */
@FeignClient(name = "analytics-service", fallback = AnalyticsServiceClientFallback.class)
public interface AnalyticsServiceClient {

    @GetMapping("/api/analytics/orders/{orderId}/track")
    List<LocationHistoryDto> getOrderTrack(@PathVariable("orderId") UUID orderId);

    @PostMapping("/api/analytics/locations/latest")
    List<LatestLocationDto> getLatestLocations(@RequestBody List<UUID> orderIds);
}