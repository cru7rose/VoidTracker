package com.example.planning_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
@Slf4j
public class EventEmitterController {

    // Mock N8N Webhook URL - in real life this would be in application.properties
    private static final String N8N_WEBHOOK_URL = "https://n8n.danxils.com/webhook/events";

    @PostMapping("/emit")
    public ResponseEntity<String> emitEvent(@RequestBody Map<String, Object> eventPayload) {
        String eventType = (String) eventPayload.getOrDefault("type", "unknown");
        log.info("EVENT EMITTED: [{}] Payload: {}", eventType, eventPayload);

        // TODO: In Phase 9+, use RestTemplate/WebClient to actually POST to
        // N8N_WEBHOOK_URL
        // For now, we just log it to simulate the "Nervous System" reacting.

        return ResponseEntity.ok("Event received and queued: " + eventType);
    }
}
