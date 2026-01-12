package com.example.planning_service.controller;

import com.example.planning_service.dto.RouteAssignmentRequestDto;
import com.example.planning_service.dto.RouteAssignmentResponseDto;
import com.example.planning_service.service.RouteAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for Route Assignment management
 * Provides CRUD operations for route assignments and publishing to drivers
 */
@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@Slf4j
public class RouteAssignmentController {

    private final RouteAssignmentService service;

    /**
     * Create a new route assignment
     * POST /api/planning/assignments
     */
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createAssignment(@RequestBody RouteAssignmentRequestDto dto) {
        log.info("Creating route assignment: {}", dto.getRouteName());
        UUID id = service.saveRouteAssignment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    /**
     * Batch create multiple route assignments
     * POST /api/planning/assignments/batch
     * Used by frontend to auto-save routes after optimization
     */
    @PostMapping("/batch")
    public ResponseEntity<List<UUID>> createBatchAssignments(@RequestBody List<RouteAssignmentRequestDto> dtos) {
        log.info("Batch creating {} route assignments", dtos.size());
        List<UUID> ids = dtos.stream()
                .map(service::saveRouteAssignment)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(ids);
    }

    /**
     * Get all route assignments
     * GET /api/planning/assignments
     */
    @GetMapping
    public ResponseEntity<List<RouteAssignmentResponseDto>> getAllAssignments() {
        List<RouteAssignmentResponseDto> assignments = service.getAllRouteAssignments();
        return ResponseEntity.ok(assignments);
    }

    /**
     * Get single route assignment by ID
     * GET /api/planning/assignments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteAssignmentResponseDto> getAssignmentById(@PathVariable UUID id) {
        RouteAssignmentResponseDto assignment = service.getRouteAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    /**
     * Update existing route assignment
     * PUT /api/planning/assignments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAssignment(
            @PathVariable UUID id,
            @RequestBody RouteAssignmentRequestDto dto) {
        log.info("Updating route assignment {}", id);
        service.updateRouteAssignment(id, dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete route assignment
     * DELETE /api/planning/assignments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID id) {
        log.info("Deleting route assignment {}", id);
        service.deleteRouteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publish route to driver (generates magic link)
     * POST /api/planning/assignments/{id}/publish
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, String>> publishRoute(@PathVariable UUID id) {
        log.info("Publishing route assignment {} to driver", id);
        String magicLink = service.publishRouteToDriver(id);
        return ResponseEntity.ok(Map.of("magicLink", magicLink));
    }

    /**
     * Exception handler for IllegalArgumentException (e.g. not found)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Exception handler for IllegalStateException (e.g. invalid state)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}
