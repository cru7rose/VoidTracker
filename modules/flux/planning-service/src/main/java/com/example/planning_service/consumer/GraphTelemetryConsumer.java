package com.example.planning_service.consumer;

import com.example.danxils_commons.event.DriverLocationUpdatedEvent;
import com.example.planning_service.repository.graph.GraphDriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class GraphTelemetryConsumer {

    private final GraphDriverRepository driverRepository;

    @org.springframework.beans.factory.annotation.Value("${app.kafka.topics.driver-location-updated}")
    private String topic;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("GraphTelemetryConsumer initialized. Listening on topic: {}", topic);
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "${app.kafka.topics.driver-location-updated}", partitions = "0"), groupId = "planning-service-telemetry-manual")
    public void consumeDriverLocationUpdate(org.apache.kafka.clients.consumer.ConsumerRecord<String, Object> record) {
        log.info("Received telemetry record. Value type: {}", record.value().getClass().getName());
        log.info("Received telemetry value: {}", record.value());

        try {
            DriverLocationUpdatedEvent event;
            if (record.value() instanceof String) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                event = mapper.readValue((String) record.value(), DriverLocationUpdatedEvent.class);
            } else if (record.value() instanceof LinkedHashMap) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                event = mapper.convertValue(record.value(), DriverLocationUpdatedEvent.class);
            } else {
                event = (DriverLocationUpdatedEvent) record.value();
            }

            log.info("Parsed telemetry for driver: {}", event.getDriverId());

            driverRepository.findById(event.getDriverId()).ifPresent(driver -> {
                driver.setLatitude(event.getLatitude());
                driver.setLongitude(event.getLongitude());
                driver.setLastUpdate(event.getTimestamp());
                driverRepository.save(driver);
                log.info("Graph updated for driver: {}", event.getDriverId());
            });
        } catch (Exception e) {
            log.error("Failed to parse telemetry: {}", e.getMessage(), e);
        }
    }
}
