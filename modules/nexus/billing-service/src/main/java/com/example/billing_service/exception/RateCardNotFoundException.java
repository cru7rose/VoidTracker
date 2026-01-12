package com.example.billing_service.exception;

/**
 * Exception thrown when no active rate card is found for a client.
 */
public class RateCardNotFoundException extends BillingException {
    public RateCardNotFoundException(String clientId) {
        super(String.format("No active rate card found for client: %s", clientId));
    }
}
