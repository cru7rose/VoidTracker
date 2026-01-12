// File: tracking-service/src/main/java/com/example/tracking_service/service/TrackingService.java
package com.example.tracking_service.service;

import com.example.danxils_commons.event.DriverLocationUpdatedEvent;
import com.example.tracking_service.config.AppProperties;
import com.example.tracking_service.dto.LocationUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Serwis odpowiedzialny za logikę biznesową modułu śledzenia.
 * Jego jedynym zadaniem jest przyjęcie danych wejściowych z kontrolera,
 * wzbogacenie ich o informacje kontekstowe (ID kierowcy, znacznik czasu)
 * i opublikowanie kanonicznego zdarzenia domenowego do Kafki.
 * Taka architektura zapewnia wysokie oddzielenie (decoupling) i skalowalność.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TrackingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppProperties appProperties;

    // Simple in-memory cache for "Last Mile" tracking
    // Key: OrderID (as String), Value: Last Event
    private final java.util.Map<String, DriverLocationUpdatedEvent> locationCache = new java.util.concurrent.ConcurrentHashMap<>();

    public DriverLocationUpdatedEvent getLatestLocation(String orderId) {
        return locationCache.get(orderId);
    }

    public java.util.List<DriverLocationUpdatedEvent> getFleetLocations() {
        // Group by driverId and get the latest event for each driver
        return locationCache.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(DriverLocationUpdatedEvent::getDriverId))
                .values().stream()
                .map(events -> events.stream()
                        .max(java.util.Comparator.comparing(DriverLocationUpdatedEvent::getTimestamp))
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    public void processLocationUpdate(LocationUpdateRequest request, String driverId) {
        DriverLocationUpdatedEvent event = DriverLocationUpdatedEvent.builder()
                .eventId(UUID.randomUUID())
                .orderId(request.getOrderId())
                .driverId(driverId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .timestamp(Instant.now())
                .batteryLevel(request.getBatteryLevel())
                .signalStrength(request.getSignalStrength())
                .speed(request.getSpeed())
                .build();

        String topic = appProperties.getKafka().getTopics().getDriverLocationUpdated();
        String key = driverId + ":" + request.getOrderId().toString();

        // Update cache
        locationCache.put(request.getOrderId().toString(), event);

        publishEvent(topic, key, event);
    }

    public void processTerminalScan(com.example.tracking_service.dto.TerminalScanRequest request) {
        DriverLocationUpdatedEvent event = DriverLocationUpdatedEvent.builder()
                .eventId(java.util.UUID.randomUUID())
                .orderId(java.util.UUID.fromString(request.getOrderId())) // Assuming UUID for now
                .driverId("TERM:" + request.getTerminalId()) // Special ID for terminals
                .latitude(request.getLatitude() != null ? request.getLatitude() : 0.0)
                .longitude(request.getLongitude() != null ? request.getLongitude() : 0.0)
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : java.time.Instant.now())
                .build();

        // Update cache so it shows up in tracking
        locationCache.put(request.getOrderId(), event);

        // Publish to Kafka (reuse same topic or new one)
        String topic = appProperties.getKafka().getTopics().getDriverLocationUpdated();
        String key = "TERM:" + request.getTerminalId() + ":" + request.getOrderId();
        publishEvent(topic, key, event);
    }

    private void publishEvent(String topic, String key, Object event) {
        try {
            log.info("Publikacja zdarzenia {} do tematu '{}' z kluczem {}", event.getClass().getSimpleName(), topic,
                    key);
            kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Zdarzenie dla klucza {} pomyślnie opublikowane. Offset: {}", key,
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Błąd publikacji zdarzenia dla klucza {}: {}", key, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error(
                    "KRYTYCZNY BŁĄD: Nie udało się zainicjować wysyłki zdarzenia dla klucza {}. Wymagana ręczna interwencja.",
                    key, e);
            throw new RuntimeException("Błąd publikacji zdarzenia.", e);
        }
    }
}