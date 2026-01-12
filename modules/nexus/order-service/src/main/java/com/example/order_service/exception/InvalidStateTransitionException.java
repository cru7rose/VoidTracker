package com.example.order_service.exception;

import com.example.danxils_commons.enums.OrderStatus;

import java.util.Set;

/**
 * Exception thrown when an invalid state transition is attempted.
 * Uses existing OrderStatus.canTransitionTo() validation.
 * HTTP 409 Conflict
 */
public class InvalidStateTransitionException extends OrderException {

    private final OrderStatus currentStatus;
    private final OrderStatus attemptedStatus;
    private final Set<OrderStatus> allowedStatuses;

    public InvalidStateTransitionException(OrderStatus currentStatus, OrderStatus attemptedStatus,
            Set<OrderStatus> allowedStatuses) {
        super(String.format(
                "Invalid state transition from %s to %s. Allowed transitions: %s",
                currentStatus, attemptedStatus, allowedStatuses));
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
        this.allowedStatuses = allowedStatuses;
    }

    public static InvalidStateTransitionException fromTerminalState(OrderStatus terminalStatus) {
        return new InvalidStateTransitionException(
                terminalStatus,
                null,
                Set.of());
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public OrderStatus getAttemptedStatus() {
        return attemptedStatus;
    }

    public Set<OrderStatus> getAllowedStatuses() {
        return allowedStatuses;
    }
}
