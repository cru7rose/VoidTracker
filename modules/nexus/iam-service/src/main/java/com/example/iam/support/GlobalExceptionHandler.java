package com.example.iam.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler for security and validation exceptions.
 * Provides consistent error responses across all IAM endpoints.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
                log.warn("Access denied: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of(
                                                "timestamp", Instant.now().toString(),
                                                "status", 403,
                                                "error", "Forbidden",
                                                "message", "You do not have permission to access this resource"));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
                log.warn("Authentication failed: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of(
                                                "timestamp", Instant.now().toString(),
                                                "status", 401,
                                                "error", "Unauthorized",
                                                "message", "Invalid username or password"));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
                log.warn("Bad request: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(
                                                "timestamp", Instant.now().toString(),
                                                "status", 400,
                                                "error", "Bad Request",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
                if (ex.getMessage() != null
                                && (ex.getMessage().contains("exist") || ex.getMessage().contains("istnieje"))) {
                        log.warn("Conflict: {}", ex.getMessage());
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body(Map.of(
                                                        "timestamp", Instant.now().toString(),
                                                        "status", 409,
                                                        "error", "Conflict",
                                                        "message", ex.getMessage()));
                }
                log.error("Internal error: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of(
                                                "timestamp", Instant.now().toString(),
                                                "status", 500,
                                                "error", "Internal Server Error",
                                                "message", "An unexpected error occurred"));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
                log.error("Unhandled exception: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of(
                                                "timestamp", Instant.now().toString(),
                                                "status", 500,
                                                "error", "Internal Server Error",
                                                "message",
                                                ex.getMessage() != null ? ex.getMessage()
                                                                : "An unexpected error occurred",
                                                "exceptionType", ex.getClass().getSimpleName()));
        }
}
