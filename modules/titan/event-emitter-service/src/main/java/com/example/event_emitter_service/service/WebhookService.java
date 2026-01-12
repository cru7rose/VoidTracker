package com.example.event_emitter_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final RestTemplate restTemplate;
    private final String n8nBaseUrl;

    public WebhookService(RestTemplateBuilder builder, @Value("${n8n.base-url}") String n8nBaseUrl) {
        this.restTemplate = builder.build();
        this.n8nBaseUrl = n8nBaseUrl;
    }

    public void triggerWorkflow(String workflowId, String payloadJson) {
        if (workflowId == null || workflowId.isBlank()) {
            return;
        }

        String url = String.format("%s/webhook/%s", n8nBaseUrl, workflowId);
        log.info("Forwarding event to n8n: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(payloadJson, headers);

            restTemplate.postForObject(url, request, String.class);
            log.info("Successfully triggered n8n workflow: {}", workflowId);
        } catch (Exception e) {
            log.error("Failed to trigger n8n workflow {}: {}", workflowId, e.getMessage());
        }
    }
}
