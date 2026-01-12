package com.example.order_service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response DTO for Order Service.
 * Provides consistent error structure across all endpoints.
 */
@Data
// @Builder - Manual implementation below
// @NoArgsConstructor
// @AllArgsConstructor
public class ErrorResponse {
    private String timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(String timestamp, int status, String error, String message, String errorCode, String path,
            Map<String, Object> details) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.errorCode = errorCode;
        this.path = path;
        this.details = details;
    }

    public ErrorResponse(int status, String message, long timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = String.valueOf(timestamp);
    }

    // Manual Builder
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public static class ErrorResponseBuilder {
        private String timestamp;
        private int status;
        private String error;
        private String message;
        private String errorCode;
        private String path;
        private Map<String, Object> details;

        public ErrorResponseBuilder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        public ErrorResponseBuilder error(String error) {
            this.error = error;
            return this;
        }

        public ErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ErrorResponseBuilder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(timestamp, status, error, message, errorCode, path, details);
        }
    }

    private int status;
    private String error;
    private String message;
    private String errorCode;
    private String path;
    private Map<String, Object> details;

    public static ErrorResponse of(int status, String error, String message, String errorCode, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status)
                .error(error)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .build();
    }

    public static ErrorResponse of(int status, String error, String message, String errorCode, String path,
            Map<String, Object> details) {
        return ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status)
                .error(error)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .details(details)
                .build();
    }
}
