package com.example.planning_service.service;

import com.example.planning_service.entity.WebhookConfigEntity;
import com.example.planning_service.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookConfigRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<WebhookConfigEntity> getAllWebhooks() {
        return repository.findAll();
    }

    public WebhookConfigEntity saveWebhook(WebhookConfigEntity entity) {
        return repository.save(entity);
    }

    public List<WebhookConfigEntity> saveAllWebhooks(List<WebhookConfigEntity> entities) {
        return repository.saveAll(entities);
    }

    public void triggerEvent(String eventType, Map<String, Object> payload) {
        Optional<WebhookConfigEntity> config = repository.findByEventType(eventType);
        if (config.isPresent() && config.get().isActive()) {
            try {
                log.info("Triggering webhook for event: {}", eventType);

                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

                if (config.get().getAuthHeader() != null && !config.get().getAuthHeader().isEmpty()) {
                    // Assuming the authHeader contains the full value e.g. "Bearer <token>" or
                    // "ApiKey <key>"
                    // If the user just provides the key, we might need a convention.
                    // For n8n, it's often "X-N8N-API-KEY" or similar.
                    // Let's assume the user puts "HeaderName: Value" or just the value if it's a
                    // simple token?
                    // To be safe and flexible, let's assume the user enters the full header value
                    // and we put it in "Authorization"
                    // OR we can add a specific field for header name.
                    // For simplicity in this iteration, let's treat it as a generic "Authorization"
                    // header value
                    // or a specific custom header if we parse it.
                    // Let's try to detect if it has a colon.
                    String auth = config.get().getAuthHeader();
                    if (auth.contains(":")) {
                        String[] parts = auth.split(":", 2);
                        headers.set(parts[0].trim(), parts[1].trim());
                    } else {
                        // Default to Authorization header
                        headers.set("Authorization", auth);
                    }
                }

                org.springframework.http.HttpEntity<Map<String, Object>> request = new org.springframework.http.HttpEntity<>(
                        payload, headers);
                restTemplate.postForObject(config.get().getUrl(), request, String.class);
            } catch (Exception e) {
                log.error("Failed to trigger webhook for event: {}", eventType, e);
            }
        } else {
            log.debug("No active webhook configured for event: {}", eventType);
        }
    }
}
