package com.example.billing_service.exception;

/**
 * Base exception for billing service errors.
 */
public class BillingException extends RuntimeException {
    public BillingException(String message) {
        super(message);
    }
    
    public BillingException(String message, Throwable cause) {
        super(message, cause);
    }
}
