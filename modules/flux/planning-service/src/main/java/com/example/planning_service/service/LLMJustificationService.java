package com.example.planning_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating Polish language justifications using LLM API.
 * Supports OpenAI and Anthropic Claude APIs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LLMJustificationService {

    @Value("${llm.provider:openai}")
    private String llmProvider;

    @Value("${llm.openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${llm.openai.api.key:}")
    private String openaiApiKey;

    @Value("${llm.openai.model:gpt-4}")
    private String openaiModel;

    @Value("${llm.anthropic.api.url:https://api.anthropic.com/v1/messages}")
    private String anthropicApiUrl;

    @Value("${llm.anthropic.api.key:}")
    private String anthropicApiKey;

    @Value("${llm.anthropic.model:claude-3-opus-20240229}")
    private String anthropicModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate Polish justification for optimization approval request
     */
    public String generateJustification(List<String> warnings, double scoreChangePercent) {
        if (warnings == null || warnings.isEmpty()) {
            return "Brak szczegółowych ostrzeżeń.";
        }

        try {
            String prompt = buildPrompt(warnings, scoreChangePercent);
            
            if ("anthropic".equalsIgnoreCase(llmProvider)) {
                return generateWithAnthropic(prompt);
            } else {
                return generateWithOpenAI(prompt);
            }
        } catch (Exception e) {
            log.error("Failed to generate LLM justification", e);
            return buildFallbackJustification(warnings, scoreChangePercent);
        }
    }

    private String generateWithOpenAI(String prompt) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            log.warn("OpenAI API key not configured, using fallback");
            return null;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("model", openaiModel);
        request.put("messages", List.of(
                Map.of("role", "system", "content", "Jesteś ekspertem logistycznym. Odpowiadaj zawsze po polsku."),
                Map.of("role", "user", "content", prompt)
        ));
        request.put("temperature", 0.7);
        request.put("max_tokens", 300);

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + openaiApiKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> entity = 
                    new org.springframework.http.HttpEntity<>(request, headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
                    openaiApiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode choices = json.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    String content = choices.get(0).path("message").path("content").asText();
                    log.info("✅ Generated LLM justification (OpenAI)");
                    return content;
                }
            }
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
        }

        return null;
    }

    private String generateWithAnthropic(String prompt) {
        if (anthropicApiKey == null || anthropicApiKey.isEmpty()) {
            log.warn("Anthropic API key not configured, using fallback");
            return null;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("model", anthropicModel);
        request.put("max_tokens", 300);
        request.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        request.put("system", "Jesteś ekspertem logistycznym. Odpowiadaj zawsze po polsku.");

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("x-api-key", anthropicApiKey);
            headers.set("anthropic-version", "2023-06-01");
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> entity = 
                    new org.springframework.http.HttpEntity<>(request, headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
                    anthropicApiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode content = json.path("content");
                if (content.isArray() && content.size() > 0) {
                    String text = content.get(0).path("text").asText();
                    log.info("✅ Generated LLM justification (Anthropic)");
                    return text;
                }
            }
        } catch (Exception e) {
            log.error("Anthropic API call failed", e);
        }

        return null;
    }

    private String buildPrompt(List<String> warnings, double scoreChangePercent) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Wyjaśnij dlaczego ta optymalizacja tras wymaga zatwierdzenia przez dyspozytora.\n\n");
        prompt.append("Zmiana wyniku optymalizacji: ").append(String.format("%.2f", scoreChangePercent)).append("%\n\n");
        prompt.append("Ostrzeżenia:\n");
        for (String warning : warnings) {
            prompt.append("- ").append(warning).append("\n");
        }
        prompt.append("\nWyjaśnij krótko (2-3 zdania) po polsku dlaczego ta zmiana jest znacząca i wymaga przeglądu.");
        return prompt.toString();
    }

    private String buildFallbackJustification(List<String> warnings, double scoreChangePercent) {
        StringBuilder justification = new StringBuilder();
        justification.append("Optymalizacja wykryła znaczącą zmianę wyniku o ");
        justification.append(String.format("%.2f", scoreChangePercent)).append("%. ");
        justification.append("Wymaga to przeglądu przez dyspozytora przed publikacją tras. ");
        justification.append("Ostrzeżenia: ").append(String.join(", ", warnings));
        return justification.toString();
    }
}
