package com.example.planning_service.controller;

import com.example.planning_service.entity.WebhookConfigEntity;
import com.example.planning_service.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @GetMapping
    public List<WebhookConfigEntity> getAll() {
        return webhookService.getAllWebhooks();
    }

    @PostMapping
    public WebhookConfigEntity save(@RequestBody WebhookConfigEntity entity) {
        return webhookService.saveWebhook(entity);
    }

    @PostMapping("/batch")
    public List<WebhookConfigEntity> saveBatch(@RequestBody List<WebhookConfigEntity> entities) {
        return webhookService.saveAllWebhooks(entities);
    }

    @PostMapping("/test/{eventType}")
    public ResponseEntity<String> testWebhook(@PathVariable String eventType) {
        webhookService.triggerEvent(eventType, Map.of(
                "test", true,
                "message", "This is a test event from Danxils Dashboard",
                "timestamp", System.currentTimeMillis()));
        return ResponseEntity.ok("Triggered");
    }
}
