package com.example.planning_service.controller;

import com.example.planning_service.dto.DriverRouteResponseDto;
import com.example.planning_service.service.RouteAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST API for Ghost PWA (Driver Mobile App) authentication and route access.
 * Handles magic link validation and route retrieval for drivers.
 */
@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
@Slf4j
public class DriverAuthController {

    private final RouteAssignmentService routeAssignmentService;

    /**
     * Validate magic link token from Ghost PWA
     * GET /api/planning/driver/auth/validate?token={uuid}
     * 
     * Used by Ghost PWA when driver clicks magic link.
     * Returns driver ID and route ID if token is valid.
     */
    @GetMapping("/auth/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        log.info("Validating magic link token: {}", token);
        
        String driverId = routeAssignmentService.validateToken(token);
        
        if (driverId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
        }
        
        // Get route ID from token store (stored during publish)
        UUID routeId = routeAssignmentService.getRouteIdFromToken(token);
        
        return ResponseEntity.ok(Map.of(
                "driverId", driverId,
                "routeId", routeId != null ? routeId.toString() : "",
                "valid", true
        ));
    }

    /**
     * Get active route for driver
     * GET /api/planning/driver/{driverId}/route
     * 
     * Returns the most recent PUBLISHED or IN_PROGRESS route for the driver.
     * Used by Ghost PWA to display route after authentication.
     */
    @GetMapping("/{driverId}/route")
    public ResponseEntity<DriverRouteResponseDto> getDriverRoute(@PathVariable UUID driverId) {
        log.info("Fetching route for driver: {}", driverId);
        
        DriverRouteResponseDto route = routeAssignmentService.getActiveRouteForDriver(driverId);
        
        if (route == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
        
        return ResponseEntity.ok(route);
    }

    /**
     * Update stop status (for Ghost PWA)
     * POST /api/planning/driver/status
     * 
     * Body: { stopId, status, location: { lat, lon }, timestamp }
     * 
     * Used by Ghost PWA to update delivery status (ARRIVED, POD, COMPLETED, etc.)
     */
    @PostMapping("/status")
    public ResponseEntity<Map<String, String>> updateStopStatus(@RequestBody Map<String, Object> statusUpdate) {
        log.info("Updating stop status: {}", statusUpdate);
        
        // TODO: Implement status update logic
        // This will update the route_data JSONB with stop status
        
        return ResponseEntity.ok(Map.of("status", "updated"));
    }
}
