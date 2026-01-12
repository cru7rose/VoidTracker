package com.example.event_emitter_service.consumer;

import com.example.danxils_commons.event.OrderStatusChangedEvent;
import com.example.event_emitter_service.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "orders.status-changed", groupId = "event-emitter-group")
    public void handleOrderStatusChanged(String message) {
        log.debug("Received OrderStatusChangedEvent: {}", message);
        try {
            // Check if payload contains workflowId or similar metadata
            // For MVP, we parse to generic map or specific event to check triggers
            // Ideally, the event itself should carry the 'workflowId' if it was enriched by OrderService
            // OR we fetch the Order details here.
            
            // Current Plan: OrderService triggers synchronous webhook. 
            // New Plan: OrderService emits event. If we want n8n to be triggered, 
            // the Event SHOULD contain the workflowId, OR we need to lookup.
            // Looking up is expensive. Enriched event is better.
            
            // Let's assume for now we just forward EVERYTHING to a specific 'Router' workflow in n8n
            // or we decode logic if present.
            
            // Re-using OrderStatusChangedEvent from commons? 
            // It does not have workflowId.
            // Hack for MVP: We forward the whole event to a "Router Workflow" ID if configured,
            // OR we inspect the message content.
             
            // For this specific 'event-emitter', we might want to just forward raw JSON to a central N8N hook?
            // User requested: "triggerWorkflow(String workflowId...)"
            
            // If the message is just the standard event, we don't know the workflowId.
            // We need to fetch it or have it passed.
            // Let's assume we forward to a generic "Event Bus" workflow in n8n for routing?
            // Or we assume the payload includes it.
            
            // Let's optimistically attempt to read "workflowId" from the root of the JSON if present (Enriched Event)
            Map<String, Object> map = objectMapper.readValue(message, Map.class);
            if (map.containsKey("workflowId")) {
                 String wfId = (String) map.get("workflowId");
                 webhookService.triggerWorkflow(wfId, message);
            }
            
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage());
        }
    }
}
