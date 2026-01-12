package com.example.order_service.exception;

/**
 * Base exception for order service errors.
 * All custom order exceptions extend this class.
 */
public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
