package com.example.order_service.service;

import com.example.danxils_commons.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
public class TmsForwardingConsumer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TmsForwardingConsumer.class);

    private final OrderService orderService;

    /**
     * ARCHITEKTURA: Uproszczony konsument. Atrybut 'groupId' został usunięty.
     * Konsument używa teraz globalnego group.id zdefiniowanego w application.yml,
     * co zapewnia spójność konfiguracji w całym mikroserwisie.
     */
    @KafkaListener(topics = "${app.kafka.topics.orders-created}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Odebrano OrderCreatedEvent dla orderId: {}. Przekazywanie do TMS...", event.getOrderId());
        try {
            log.info("Pomyślnie przekazano zlecenie {} do TMS (symulacja).", event.getOrderId());
            orderService.markAsForwardedToTms(event.getOrderId());

        } catch (Exception e) {
            log.error("Nie udało się przekazać zlecenia {} do TMS. Błąd: {}. Wiadomość zostanie ponowiona.",
                    event.getOrderId(), e.getMessage());
            throw new RuntimeException("Przekazanie do TMS nie powiodło się", e);
        }
    }
}