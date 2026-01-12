package com.example.analytics_service.consumer;

import com.example.analytics_service.service.AnalyticsService;
import com.example.danxils_commons.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import com.example.danxils_commons.event.DriverLocationUpdatedEvent;

/**
 * ARCHITEKTURA: Konsument zdarzeń Kafki dla serwisu analitycznego.
 * Został uproszczony poprzez usunięcie atrybutów 'containerFactory'.
 * Teraz używa domyślnej, globalnej fabryki tworzonej automatycznie przez Spring Boot,
 * co zapewnia spójność i niezawodność konfiguracji.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AnalyticsConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "#{'${app.kafka.topics.orders-created}'}")
    public void handleOrderCreated(@Payload OrderCreatedEvent event) {
        log.debug("AnalyticsConsumer: Otrzymano OrderCreatedEvent dla zlecenia: {}", event.getOrderId());
        processEvent(() -> analyticsService.processOrderCreation(event), event.getOrderId());
    }

    @KafkaListener(topics = "#{'${app.kafka.topics.order-status-changed}'}")
    public void handleOrderStatusChanged(@Payload OrderStatusChangedEvent event) {
        log.debug("AnalyticsConsumer: Otrzymano OrderStatusChangedEvent dla zlecenia: {}", event.getOrderId());
        processEvent(() -> analyticsService.processOrderStatusChange(event), event.getOrderId());
    }

    @KafkaListener(topics = "#{'${app.kafka.topics.orders-assigned}'}")
    public void handleOrderAssigned(@Payload OrderAssignedEvent event) {
        log.debug("AnalyticsConsumer: Otrzymano OrderAssignedEvent dla zlecenia: {}", event.getOrderId());
        processEvent(() -> analyticsService.processOrderAssignment(event), event.getOrderId());
    }

    @KafkaListener(topics = "#{'${app.kafka.topics.route-planned}'}")
    public void handleRoutePlanned(@Payload RoutePlannedEvent event) {
        log.debug("AnalyticsConsumer: Otrzymano RoutePlannedEvent dla planu: {}", event.getPlanId());
        processEvent(() -> analyticsService.processRoutePlanning(event), event.getPlanId().toString());
    }

    @KafkaListener(topics = "#{'${app.kafka.topics.driver-location-updated}'}")
    public void handleDriverLocationUpdated(@Payload DriverLocationUpdatedEvent event) {
        log.debug("AnalyticsConsumer: Otrzymano DriverLocationUpdatedEvent dla zlecenia: {}", event.getOrderId());
        processEvent(() -> analyticsService.processDriverLocationUpdate(event), event.getOrderId().toString());
    }

    private void processEvent(Runnable logic, String eventKey) {
        try {
            logic.run();
        } catch (Exception e) {
            log.error("Błąd w AnalyticsConsumer podczas przetwarzania zdarzenia (klucz: {}). Wiadomość zostanie przekierowana do DLT.", eventKey, e);
            throw new RuntimeException("Błąd przetwarzania zdarzenia w AnalyticsConsumer dla klucza: " + eventKey, e);
        }
    }
}