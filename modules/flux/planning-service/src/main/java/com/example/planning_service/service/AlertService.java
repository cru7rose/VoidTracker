package com.example.planning_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendSlaViolationAlert(UUID routeId, UUID orderId, LocalDateTime predictedArrival,
            LocalDateTime slaEnd) {
        String message = String.format("SLA Violation Detected! Route: %s, Order: %s. Predicted: %s, SLA End: %s",
                routeId, orderId, predictedArrival, slaEnd);

        log.warn(message);

        // In a real implementation, we would send a structured event object.
        // using a string/map for now as per the "shim" approach until proper DTOs are
        // shared.
        kafkaTemplate.send("crm.alerts", message);
    }
}
