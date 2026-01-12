package com.example.audit_service.consumer;

import com.example.danxils_commons.event.OrderAssignedEvent;
import com.example.danxils_commons.event.OrderStatusChangedEvent;
import com.example.audit_service.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * ARCHITEKTURA: Konsument Kafki dedykowany do nasłuchiwania na zdarzenia
 * pochodzące z domeny zleceń (Order). Każda metoda jest odpowiedzialna za
 * odbiór konkretnego typu zdarzenia i przekazanie go do serwisu audytu
 * w celu zapisania w historii.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventsConsumer {

    private final AuditService auditService;

    private static final String ENTITY_TYPE_ORDER = "ORDER";
    private static final String SERVICE_NAME_ORDER = "order-service";

    @KafkaListener(topics = "#{'${app.kafka.topics.orders-status-changed}'}")
    public void handleOrderStatusChanged(@Payload OrderStatusChangedEvent event) {
        log.debug("Consumed OrderStatusChangedEvent for orderId: {}", event.getOrderId());
        auditService.recordEvent(
                "ORDER_STATUS_CHANGED",
                ENTITY_TYPE_ORDER,
                event.getOrderId(),
                event.getChangedBy(),
                SERVICE_NAME_ORDER,
                event
        );
    }

    @KafkaListener(topics = "#{'${app.kafka.topics.orders-assigned}'}")
    public void handleOrderAssigned(@Payload OrderAssignedEvent event) {
        log.debug("Consumed OrderAssignedEvent for orderId: {}", event.getOrderId());
        auditService.recordEvent(
                "ORDER_ASSIGNED",
                ENTITY_TYPE_ORDER,
                event.getOrderId(),
                event.getAssignedBy(),
                SERVICE_NAME_ORDER,
                event
        );
    }
}