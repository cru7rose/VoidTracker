package com.example.order_service.consumer;

import com.example.danxils_commons.event.RoutePlannedEvent;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanningEventConsumer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlanningEventConsumer.class);

    private final OrderService orderService;

    /**
     * ARCHITEKTURA: Uproszczony konsument. Atrybut 'containerFactory' został
     * usunięty.
     * Konsument używa teraz domyślnej, globalnej fabryki tworzonej automatycznie
     * przez Spring Boot, co zapewnia spójność i niezawodność.
     */
    @KafkaListener(topics = "#{'${app.kafka.topics.route-planned}'}")
    public void handleRoutePlanned(@Payload RoutePlannedEvent event) {
        log.info("Odebrano zdarzenie RoutePlannedEvent dla planu: {}. Ilość zleceń: {}", event.getPlanId(),
                event.getIncludedOrderIds().size());
        try {
            orderService.processRoutePlan(event);
        } catch (Exception e) {
            log.error(
                    "Błąd podczas przetwarzania zdarzenia RoutePlannedEvent dla planu {}. Wiadomość zostanie przekierowana do DLT.",
                    event.getPlanId(),
                    e);
            throw new RuntimeException("Nie udało się przetworzyć zdarzenia RoutePlannedEvent", e);
        }
    }
}