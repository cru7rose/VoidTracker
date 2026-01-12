package com.example.order_service.service;

import com.example.order_service.config.AppProperties;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebhookService.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    public WebhookService(RestTemplate restTemplate, AppProperties appProperties) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
    }

    public void triggerWorkflow(String workflowId, Object payload) {
        if (workflowId == null || workflowId.isBlank()) {
            log.warn("Workflow ID is empty, skipping webhook trigger.");
            return;
        }

        String url = String.format("%s/webhook/%s", appProperties.getN8nUrl(), workflowId);
        log.info("Triggering n8n workflow at URL: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(url, request, String.class);
            log.info("Successfully triggered n8n workflow: {}", workflowId);
        } catch (Exception e) {
            log.error("Failed to trigger n8n workflow: {}. Error: {}", workflowId, e.getMessage());
            // We don't throw exception here to avoid rolling back the transaction
            // Webhook failure should not block the main business process
        }
    }
}
