package com.example.billing_service.exception;

/**
 * Exception thrown when order context has invalid data.
 */
public class InvalidOrderContextException extends BillingException {
    public InvalidOrderContextException(String message) {
        super(message);
    }
    
    public static InvalidOrderContextException negativeValue(String field, Object value) {
        return new InvalidOrderContextException(
            String.format("Invalid %s: %s. Values must be non-negative.", field, value));
    }
    
    public static InvalidOrderContextException missingRequired(String field) {
        return new InvalidOrderContextException(
            String.format("Required field missing: %s", field));
    }
}
