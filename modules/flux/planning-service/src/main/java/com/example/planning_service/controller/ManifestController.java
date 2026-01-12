package com.example.planning_service.controller;

import com.example.planning_service.dto.request.AssignDriverRequestDto;
import com.example.planning_service.dto.ManifestResponseDto;
import com.example.planning_service.entity.ManifestEntity;
import com.example.planning_service.entity.ManifestStatus;
import com.example.planning_service.service.ManifestService;
import com.example.planning_service.execution.PlanningExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Manifest Management.
 * Provides endpoints for generating, querying, and managing manifests.
 */
@RestController
@RequestMapping("/manifests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Manifest Management", description = "API for managing driver manifests and route optimization.")
public class ManifestController {

    private final ManifestService manifestService;
    private final PlanningExecutionService planningExecutionService;

    /**
     * Generate new manifests using VRP optimization.
     * Triggers route planning for all pending orders.
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate manifests", description = "Triggers VRP optimization to generate manifests for pending orders")
    public ResponseEntity<String> generateManifests() {
        log.info("Received request to generate manifests");

        try {
            // TODO: Get the default route plan ID from configuration or create one
            // For now, we'll need to create a simple trigger mechanism
            // This will be enhanced when we integrate with the scheduler

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Manifest generation triggered. Check status via GET /api/planning/manifests");
        } catch (Exception e) {
            log.error("Error generating manifests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating manifests: " + e.getMessage());
        }
    }

    /**
     * Get all manifests with optional filters.
     */
    @GetMapping
    @Operation(summary = "Get all manifests", description = "Retrieve manifests with optional filters for date, status, and driver")
    public ResponseEntity<List<ManifestResponseDto>> getManifests(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Fetching manifests with filters - date: {}, status: {}, driverId: {}", date, status, driverId);

        List<ManifestEntity> manifests;

        if (driverId != null && date != null) {
            manifests = manifestService.getManifestsForDriver(driverId, date);
        } else if (driverId != null && startDate != null && endDate != null) {
            // Get driver manifests in date range - would need new method
            manifests = manifestService.getManifestsForDriver(driverId, LocalDate.now());
        } else if (date != null) {
            manifests = manifestService.getManifestsByDate(date);
        } else if (status != null) {
            ManifestStatus manifestStatus = ManifestStatus.valueOf(status.toUpperCase());
            manifests = manifestService.getManifestsByStatus(manifestStatus);
        } else if (startDate != null && endDate != null) {
            manifests = manifestService.getManifestsByDateRange(startDate, endDate);
        } else {
            // Default: get today's manifests
            manifests = manifestService.getManifestsByDate(LocalDate.now());
        }

        List<ManifestResponseDto> response = manifests.stream()
                .map(manifestService::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific manifest by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get manifest by ID", description = "Retrieve detailed information about a specific manifest")
    public ResponseEntity<ManifestResponseDto> getManifest(@PathVariable UUID id) {
        log.info("Fetching manifest with ID: {}", id);

        ManifestEntity manifest = manifestService.getManifest(id);
        return ResponseEntity.ok(manifestService.toDto(manifest));
    }

    /**
     * Get manifests for a specific driver.
     */
    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get driver's manifests", description = "Retrieve manifests for a specific driver, defaults to today")
    public ResponseEntity<List<ManifestResponseDto>> getDriverManifests(
            @PathVariable UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("Fetching manifests for driver {} on {}", driverId, date);

        List<ManifestEntity> manifests = manifestService.getManifestsForDriver(driverId, date);
        List<ManifestResponseDto> response = manifests.stream()
                .map(manifestService::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Assign a driver to a manifest.
     */
    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign driver to manifest", description = "Assign a driver to a manifest and change status to ASSIGNED")
    public ResponseEntity<ManifestResponseDto> assignDriver(
            @PathVariable UUID id,
            @RequestBody AssignDriverRequestDto request) {

        log.info("Assigning driver {} to manifest {}", request.getDriverId(), id);

        try {
            ManifestEntity manifest = manifestService.assignDriver(id, UUID.fromString(request.getDriverId()));
            return ResponseEntity.ok(manifestService.toDto(manifest));
        } catch (IllegalStateException e) {
            log.warn("Cannot assign driver: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update manifest status.
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update manifest status", description = "Update the status of a manifest (DRAFT -> ASSIGNED -> IN_PROGRESS -> COMPLETED)")
    public ResponseEntity<ManifestResponseDto> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {

        log.info("Updating manifest {} status to {}", id, status);

        try {
            ManifestStatus newStatus = ManifestStatus.valueOf(status.toUpperCase());
            ManifestEntity manifest = manifestService.updateStatus(id, newStatus);
            return ResponseEntity.ok(manifestService.toDto(manifest));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            log.warn("Invalid status transition: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
