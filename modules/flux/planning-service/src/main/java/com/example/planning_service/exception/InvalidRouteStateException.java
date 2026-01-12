package com.example.planning_service.exception;

/**
 * Exception thrown when invalid route state transition is attempted.
 * HTTP 409 Conflict
 */
public class InvalidRouteStateException extends PlanningException {
    public InvalidRouteStateException(String currentState, String attemptedState) {
        super(String.format("Invalid state transition from %s to %s", currentState, attemptedState));
    }

    public InvalidRouteStateException(String message) {
        super(message);
    }

    public static InvalidRouteStateException cannotModifyCompleted(String routeId) {
        return new InvalidRouteStateException(
                String.format("Cannot modify route %s - route is already completed", routeId));
    }

    public static InvalidRouteStateException notAssigned(String routeId) {
        return new InvalidRouteStateException(
                String.format("Route %s is not assigned to a driver", routeId));
    }
}
