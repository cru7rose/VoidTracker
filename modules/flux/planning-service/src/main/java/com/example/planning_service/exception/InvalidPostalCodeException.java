package com.example.planning_service.exception;

/**
 * Exception thrown when postal code format is invalid.
 * HTTP 400 Bad Request
 */
public class InvalidPostalCodeException extends PlanningException {
    public InvalidPostalCodeException(String postalCode, String expectedFormat) {
        super(String.format("Invalid postal code: %s. Expected format: %s", postalCode, expectedFormat));
    }

    public InvalidPostalCodeException(String message) {
        super(message);
    }

    public static InvalidPostalCodeException polishFormat(String postalCode) {
        return new InvalidPostalCodeException(postalCode, "Polish postal code format: XX-XXX (e.g., 00-001)");
    }

    public static InvalidPostalCodeException nullOrEmpty() {
        return new InvalidPostalCodeException("Postal code cannot be null or empty");
    }
}
