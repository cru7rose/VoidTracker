package com.example.dashboard_service.client.fallback;

import com.example.danxils_commons.dto.LatestLocationDto;
import com.example.danxils_commons.dto.LocationHistoryDto;
import com.example.dashboard_service.client.AnalyticsServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class AnalyticsServiceClientFallback implements AnalyticsServiceClient {

    @Override
    public List<LocationHistoryDto> getOrderTrack(UUID orderId) {
        log.error("CIRCUIT BREAKER: Fallback for getOrderTrack triggered for order {}. Analytics-service is unavailable. Returning empty list.", orderId);
        return Collections.emptyList();
    }

    @Override
    public List<LatestLocationDto> getLatestLocations(List<UUID> orderIds) {
        log.error("CIRCUIT BREAKER: Fallback for getLatestLocations triggered. Analytics-service is unavailable. Returning empty list.");
        return Collections.emptyList();
    }
}