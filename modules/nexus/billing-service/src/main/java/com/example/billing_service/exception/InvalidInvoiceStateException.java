package com.example.billing_service.exception;

/**
 * Exception thrown when invoice has invalid state transition.
 */
public class InvalidInvoiceStateException extends BillingException {
    public InvalidInvoiceStateException(String message) {
        super(message);
    }
}
