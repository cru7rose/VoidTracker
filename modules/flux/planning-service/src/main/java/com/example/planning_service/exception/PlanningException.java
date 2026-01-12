package com.example.planning_service.exception;

/**
 * Base exception for planning service errors.
 * All custom planning exceptions extend this class.
 */
public class PlanningException extends RuntimeException {
    public PlanningException(String message) {
        super(message);
    }

    public PlanningException(String message, Throwable cause) {
        super(message, cause);
    }
}
