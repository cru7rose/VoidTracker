// File: dashboard-service/src/main/java/com/example/dashboard_service/consumer/LocationUpdateConsumer.java
package com.example.dashboard_service.consumer;

import com.example.danxils_commons.event.DriverLocationUpdatedEvent;
import com.example.dashboard_service.service.WebSocketBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * ARCHITEKTURA: Konsument Kafki, który nasłuchuje na zdarzenia o aktualizacji
 * lokalizacji.
 * Jego jedyną odpowiedzialnością jest natychmiastowe przekazanie otrzymanego
 * zdarzenia do `WebSocketBroadcastService`, który roześle je do podłączonych
 * klientów.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LocationUpdateConsumer {

    private final WebSocketBroadcastService webSocketBroadcastService;

    @KafkaListener(topics = "driver-location-updated", groupId = "dashboard-service")
    public void handleLocationUpdate(DriverLocationUpdatedEvent event) {
        webSocketBroadcastService.broadcastLocationUpdate(event);
    }
}