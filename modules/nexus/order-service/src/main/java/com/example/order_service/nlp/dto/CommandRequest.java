package com.example.order_service.nlp.dto;

/**
 * Command request from Oracle frontend
 */
public record CommandRequest(
        String query,
        String locale // "pl" or "en"
) {
    public CommandRequest {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }
        if (locale == null) {
            locale = "en"; // Default to English
        }
    }
}
