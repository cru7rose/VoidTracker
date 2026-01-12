package com.example.planning_service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response DTO.
 * Provides consistent error structure across all planning service endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String timestamp;
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
