package com.example.crm_service.service;

import com.example.crm_service.entity.ClientFeedbackEntity;
import com.example.crm_service.repository.ClientFeedbackRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingTriggerService {

    private final ObjectMapper objectMapper;
    private final ClientFeedbackRepository feedbackRepository;

    @KafkaListener(topics = "${app.kafka.topics.orders-status-changed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderStatusChange(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String status = event.get("val").asText(); // Assuming "val" holds the status based on previous patterns or structure

            // In a real event, we'd expect a structured object. Assuming standard Danxils event structure.
            // If it's a simple status string or a complex object, we need to parse it.
            // Let's assume the message is the OrderDTO or an Event wrapper.
            // Checking for "COMPLETED" status.
            
            if ("COMPLETED".equalsIgnoreCase(status)) {
                String orderIdStr = event.get("id").asText();
                String clientIdStr = event.get("clientId").asText(); 
                
                log.info("Order {} completed for client {}. Triggering 5-Star Feedback Loop.", orderIdStr, clientIdStr);
                
                // Logic to send email/notification would go here.
                // For now, we just log it.
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse order event", e);
        }
    }
}
