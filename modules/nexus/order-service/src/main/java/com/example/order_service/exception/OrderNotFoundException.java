package com.example.order_service.exception;

import java.util.UUID;

/**
 * Exception thrown when an order is not found.
 * HTTP 404 Not Found
 */
public class OrderNotFoundException extends OrderException {
    public OrderNotFoundException(UUID orderId) {
        super(String.format("Order not found with ID: %s", orderId));
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
