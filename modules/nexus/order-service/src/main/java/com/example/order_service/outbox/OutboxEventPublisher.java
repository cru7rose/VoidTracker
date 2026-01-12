package com.example.order_service.outbox;

import com.example.order_service.entity.OutboxEventEntity;
import com.example.order_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ARCHITEKTURA: Komponent typu "Polling Publisher" dla wzorca Transactional
 * Outbox.
 * Jego jedynym zadaniem jest cykliczne odpytywanie tabeli `outbox_events`,
 * publikowanie nieprzetworzonych zdarzeń do Kafki i usuwanie ich z tabeli
 * po pomyślnej wysyłce. Działa w osobnych transakcjach, aby zapewnić
 * niezawodność i odporność na błędy.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.polling.interval-ms:10000}")
    @Transactional
    public void publishEvents() {
        // Pobieramy tylko ograniczoną liczbę zdarzeń, aby uniknąć przeciążenia
        List<OutboxEventEntity> events = outboxEventRepository.findByOrderByCreatedAtAsc(PageRequest.of(0, 100));

        if (events.isEmpty()) {
            return;
        }

        log.info("Found {} events in outbox to publish.", events.size());
        for (OutboxEventEntity event : events) {
            try {
                // Kluczem partycjonowania jest ID agregatu (np. orderId)
                kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
                outboxEventRepository.delete(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event with ID {}. It will be retried. Error: {}", event.getId(),
                        e.getMessage());
                // Przerwanie pętli i rollback transakcji.
                // Błędne zdarzenie nie zostanie usunięte i będzie ponowiona próba w następnym
                // cyklu.
                throw new RuntimeException("Failed to publish event to Kafka", e);
            }
        }
        log.info("Successfully published and deleted {} events from outbox.", events.size());
    }
}