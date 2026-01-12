// File: dashboard-service/src/main/java/com/example/dashboard_service/service/WebSocketBroadcastService.java
package com.example.dashboard_service.service;

import com.example.danxils_commons.event.DriverLocationUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * ARCHITEKTURA: Serwis odpowiedzialny za rozsyłanie wiadomości przez WebSocket.
 * Działa jako fasada, hermetyzując logikę komunikacji z brokerem STOMP.
 * Używa `SimpMessagingTemplate` do wysyłania payloadu na określony temat.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastLocationUpdate(DriverLocationUpdatedEvent event) {
        String destination = "/topic/driver-locations";
        log.debug("Broadcasting location update for driver {} to WebSocket topic {}", event.getDriverId(), destination);

        // Wysłanie zdarzenia do wszystkich subskrybentów tematu
        messagingTemplate.convertAndSend(destination, event);
    }
}