package com.example.order_service.exception;

import java.util.Map;

/**
 * Exception thrown when order validation fails.
 * HTTP 400 Bad Request
 */
public class OrderValidationException extends OrderException {

    private final Map<String, String> validationErrors;

    public OrderValidationException(String message) {
        super(message);
        this.validationErrors = null;
    }

    public OrderValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public static OrderValidationException missingRequiredField(String fieldName) {
        return new OrderValidationException(
                String.format("Required field missing: %s", fieldName));
    }

    public static OrderValidationException invalidTimeWindow() {
        return new OrderValidationException(
                "Invalid time window: pickup/delivery times are inconsistent");
    }

    public static OrderValidationException missingCustomer() {
        return new OrderValidationException(
                "Order must have an ordering customer");
    }

    public static OrderValidationException missingAddress() {
        return new OrderValidationException(
                "Order must have both pickup and delivery addresses");
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
