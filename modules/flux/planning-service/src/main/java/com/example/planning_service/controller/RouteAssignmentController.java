package com.example.planning_service.controller;

import com.example.planning_service.dto.RouteAssignmentFilterDto;
import com.example.planning_service.dto.RouteAssignmentRequestDto;
import com.example.planning_service.dto.RouteAssignmentResponseDto;
import com.example.planning_service.entity.RouteAssignmentEntity.RouteStatus;
import com.example.planning_service.service.RouteAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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
     * Batch create multiple route assignments (optimized for high volume)
     * POST /api/planning/assignments/batch
     * Used by frontend to auto-save routes after optimization.
     * 
     * This endpoint uses bulk insert for better performance with thousands of orders.
     * Supports up to 1000 assignments per batch (configurable).
     */
    @PostMapping("/batch")
    public ResponseEntity<List<UUID>> createBatchAssignments(@RequestBody List<RouteAssignmentRequestDto> dtos) {
        log.info("Batch creating {} route assignments", dtos.size());
        
        // Validate batch size (prevent memory issues)
        if (dtos.size() > 1000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of());
        }
        
        List<UUID> ids = service.saveBatchRouteAssignments(dtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(ids);
    }

    /**
     * Get filtered and paginated route assignments
     * GET /api/planning/assignments
     * 
     * Query parameters:
     * - status: Filter by status (DRAFT, ASSIGNED, PUBLISHED, etc.) - can be repeated
     * - driverId: Filter by driver UUID
     * - vehicleId: Filter by vehicle UUID
     * - carrierId: Filter by carrier UUID
     * - solutionId: Filter by optimization solution UUID
     * - createdAfter: Filter routes created after this timestamp (ISO-8601)
     * - createdBefore: Filter routes created before this timestamp
     * - updatedAfter: Filter routes updated after this timestamp
     * - updatedBefore: Filter routes updated before this timestamp
     * - routeNameContains: Partial match on route name (case-insensitive)
     * - page: Page number (0-indexed, default: 0)
     * - size: Page size (default: 20, max: 100)
     * - sort: Sort field and direction (e.g., "createdAt,desc")
     * 
     * Example: GET /api/planning/assignments?status=ASSIGNED&status=PUBLISHED&driverId=xxx&page=0&size=50&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<RouteAssignmentResponseDto>> getAssignments(
            @RequestParam(required = false) List<RouteStatus> status,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID carrierId,
            @RequestParam(required = false) UUID solutionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedBefore,
            @RequestParam(required = false) String routeNameContains,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        
        RouteAssignmentFilterDto filter = RouteAssignmentFilterDto.builder()
                .statuses(status)
                .driverId(driverId)
                .vehicleId(vehicleId)
                .carrierId(carrierId)
                .solutionId(solutionId)
                .createdAfter(createdAfter)
                .createdBefore(createdBefore)
                .updatedAfter(updatedAfter)
                .updatedBefore(updatedBefore)
                .routeNameContains(routeNameContains)
                .build();
        
        Page<RouteAssignmentResponseDto> page = service.findFiltered(filter, pageable);
        return ResponseEntity.ok(page);
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
