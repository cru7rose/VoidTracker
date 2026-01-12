package com.example.billing_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for billing service.
 * Returns consistent error responses to clients.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RateCardNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRateCardNotFound(RateCardNotFoundException ex) {
        log.error("Rate card not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "RATE_CARD_NOT_FOUND");
    }

    @ExceptionHandler(InvalidOrderContextException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOrderContext(InvalidOrderContextException ex) {
        log.warn("Invalid order context: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_ORDER_CONTEXT");
    }

    @ExceptionHandler(InvalidInvoiceStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidInvoiceState(InvalidInvoiceStateException ex) {
        log.warn("Invalid invoice state: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), "INVALID_INVOICE_STATE");
    }

    @ExceptionHandler(BillingException.class)
    public ResponseEntity<Map<String, Object>> handleBillingException(BillingException ex) {
        log.error("Billing error: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "BILLING_ERROR");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_ARGUMENT");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support.",
                "INTERNAL_ERROR");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message, String errorCode) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        error.put("errorCode", errorCode);
        
        return ResponseEntity.status(status).body(error);
    }
}
