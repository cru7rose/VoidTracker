package com.example.planning_service.exception;

/**
 * Exception thrown when no zone is found for a given postal code.
 * HTTP 404 Not Found
 */
public class ZoneNotFoundException extends PlanningException {
    public ZoneNotFoundException(String countryCode, String postalCode) {
        super(String.format("No zone found for postal code: %s in country: %s", postalCode, countryCode));
    }

    public ZoneNotFoundException(String message) {
        super(message);
    }
}
