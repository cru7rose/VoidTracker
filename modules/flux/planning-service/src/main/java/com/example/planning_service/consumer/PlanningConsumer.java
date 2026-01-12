// File: planning-service/src/main/java/com/example/planning_service/consumer/PlanningConsumer.java
package com.example.planning_service.consumer;

import com.example.danxils_commons.event.OrderCreatedEvent;
import com.example.planning_service.service.PlanningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * ARCHITEKTURA: Konsument Kafki nasłuchujący na zdarzenia o utworzeniu nowych
 * zleceń.
 * Został zaktualizowany, aby korzystać z dedykowanej, odpornej na błędy
 * fabryki `orderCreatedEventKafkaListenerContainerFactory`.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PlanningConsumer {

    private final PlanningService planningService;

    @KafkaListener(topics = "#{'${app.kafka.topics.orders-created}'}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Odebrano zdarzenie OrderCreatedEvent (orderId: {}) do zaplanowania.", event.getOrderId());
        try {
            planningService.createPlanForNewOrder(event);
        } catch (Exception e) {
            log.error(
                    "Błąd podczas przetwarzania zlecenia {} w module planowania. Wiadomość zostanie przekierowana do DLT.",
                    event.getOrderId(), e);
            throw new RuntimeException("Błąd w module planowania", e);
        }
    }
}