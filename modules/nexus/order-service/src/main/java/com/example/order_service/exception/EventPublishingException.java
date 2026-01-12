package com.example.order_service.exception;

/**
 * Exception thrown when event publishing fails.
 * HTTP 500 Internal Server Error
 */
public class EventPublishingException extends OrderException {

    private final String eventType;
    private final String orderId;

    public EventPublishingException(String eventType, String orderId, Throwable cause) {
        super(String.format("Failed to publish %s event for order %s", eventType, orderId), cause);
        this.eventType = eventType;
        this.orderId = orderId;
    }

    public EventPublishingException(String message) {
        super(message);
        this.eventType = null;
        this.orderId = null;
    }

    public String getEventType() {
        return eventType;
    }

    public String getOrderId() {
        return orderId;
    }
}
