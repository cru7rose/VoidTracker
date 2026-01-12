package com.example.planning_service.service;

import com.example.planning_service.dto.RouteAssignmentRequestDto;
import com.example.planning_service.dto.RouteAssignmentResponseDto;
import com.example.planning_service.entity.OptimizationSolutionEntity;
import com.example.planning_service.entity.RouteAssignmentEntity;
import com.example.planning_service.repository.OptimizationSolutionRepository;
import com.example.planning_service.repository.RouteAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for route assignment and driver notification via magic links.
 * Handles CRUD operations for route assignments and generates tokens for driver
 * PWA access.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteAssignmentService {

    private final OptimizationSolutionRepository solutionRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final Map<String, MagicLinkToken> tokenStore = new HashMap<>();

    // ========== CRUD Operations ==========

    /**
     * Save a new route assignment
     */
    @Transactional
    public UUID saveRouteAssignment(RouteAssignmentRequestDto dto) {
        RouteAssignmentEntity entity = RouteAssignmentEntity.builder()
                .optimizationSolutionId(dto.getOptimizationSolutionId())
                .vehicleId(dto.getVehicleId())
                .driverId(dto.getDriverId())
                .carrierId(dto.getCarrierId())
                .routeName(dto.getRouteName())
                .routeData(dto.getRouteData())
                .status(parseStatus(dto.getStatus()))
                .build();

        RouteAssignmentEntity saved = routeAssignmentRepository.save(entity);
        log.info("Created route assignment {} with name '{}'", saved.getId(), saved.getRouteName());
        return saved.getId();
    }

    /**
     * Get all route assignments with enriched data
     */
    public List<RouteAssignmentResponseDto> getAllRouteAssignments() {
        return routeAssignmentRepository.findAll()
                .stream()
                .map(this::enrichResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get single route assignment by ID
     */
    public RouteAssignmentResponseDto getRouteAssignmentById(UUID id) {
        return routeAssignmentRepository.findById(id)
                .map(this::enrichResponse)
                .orElseThrow(() -> new IllegalArgumentException("Route assignment not found: " + id));
    }

    /**
     * Update existing route assignment
     */
    @Transactional
    public void updateRouteAssignment(UUID id, RouteAssignmentRequestDto dto) {
        RouteAssignmentEntity entity = routeAssignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route assignment not found: " + id));

        // Update fields
        if (dto.getVehicleId() != null)
            entity.setVehicleId(dto.getVehicleId());
        if (dto.getDriverId() != null)
            entity.setDriverId(dto.getDriverId());
        if (dto.getCarrierId() != null)
            entity.setCarrierId(dto.getCarrierId());
        if (dto.getRouteName() != null)
            entity.setRouteName(dto.getRouteName());
        if (dto.getRouteData() != null)
            entity.setRouteData(dto.getRouteData());
        if (dto.getStatus() != null)
            entity.setStatus(parseStatus(dto.getStatus()));

        routeAssignmentRepository.save(entity);
        log.info("Updated route assignment {}", id);
    }

    /**
     * Delete route assignment
     */
    @Transactional
    public void deleteRouteAssignment(UUID id) {
        if (!routeAssignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Route assignment not found: " + id);
        }
        routeAssignmentRepository.deleteById(id);
        log.info("Deleted route assignment {}", id);
    }

    /**
     * Publish route to driver (generates magic link and updates status)
     */
    @Transactional
    public String publishRouteToDriver(UUID assignmentId) {
        RouteAssignmentEntity assignment = routeAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Route assignment not found: " + assignmentId));

        if (assignment.getDriverId() == null) {
            throw new IllegalStateException("Cannot publish route without assigned driver");
        }

        // Generate magic link
        String driverIdStr = assignment.getDriverId().toString();
        String magicLink = generateMagicLink(driverIdStr, "driver@example.com", assignmentId);

        // Update status to PUBLISHED
        assignment.setStatus(RouteAssignmentEntity.RouteStatus.PUBLISHED);
        routeAssignmentRepository.save(assignment);

        log.info("Published route assignment {} to driver {}", assignmentId, driverIdStr);
        return magicLink;
    }

    // ========== Helper Methods ==========

    /**
     * Enrich response DTO with external data (driver/vehicle names)
     * TODO: Call IAM service and danxils-mesh to get names
     */
    private RouteAssignmentResponseDto enrichResponse(RouteAssignmentEntity entity) {
        RouteAssignmentResponseDto dto = RouteAssignmentResponseDto.builder()
                .id(entity.getId())
                .optimizationSolutionId(entity.getOptimizationSolutionId())
                .vehicleId(entity.getVehicleId())
                .driverId(entity.getDriverId())
                .carrierId(entity.getCarrierId())
                .routeName(entity.getRouteName())
                .routeData(entity.getRouteData())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .version(entity.getVersion())
                .build();

        // Enrich with names (mock for now)
        if (entity.getVehicleId() != null) {
            dto.setVehicleName("Vehicle " + entity.getVehicleId().toString().substring(0, 8));
        }
        if (entity.getDriverId() != null) {
            dto.setDriverName("Driver " + entity.getDriverId().toString().substring(0, 8));
        }

        // Extract computed fields from routeData
        if (entity.getRouteData() != null) {
            Object activities = entity.getRouteData().get("activities");
            if (activities instanceof List) {
                dto.setStopCount(((List<?>) activities).size());
            }
            Object distance = entity.getRouteData().get("totalDistance");
            if (distance instanceof Number) {
                dto.setTotalDistanceKm(((Number) distance).doubleValue() / 1000.0);
            }
        }

        return dto;
    }

    private RouteAssignmentEntity.RouteStatus parseStatus(String status) {
        if (status == null)
            return RouteAssignmentEntity.RouteStatus.DRAFT;
        try {
            return RouteAssignmentEntity.RouteStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status '{}', defaulting to DRAFT", status);
            return RouteAssignmentEntity.RouteStatus.DRAFT;
        }
    }

    // ========== Magic Link Methods (existing) ==========

    /**
     * Generate magic link for driver to access their route in PWA
     * 
     * @param driverId Driver's unique ID from IAM
     * @param contact  Email or phone number for notification
     * @param routeId  Optional route/solution ID to include in link
     * @return Magic link URL
     */
    public String generateMagicLink(String driverId, String contact, UUID routeId) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);

        MagicLinkToken linkToken = new MagicLinkToken(driverId, routeId, expiresAt);
        tokenStore.put(token, linkToken);

        String magicLinkUrl = String.format("https://driver.voidtracker.app/auth?token=%s", token);

        // Send notification (stub - implement with email/SMS provider)
        sendNotification(contact, driverId, magicLinkUrl);

        log.info("Generated magic link for driver {} with token {}, expires at {}",
                driverId, token, expiresAt);

        return magicLinkUrl;
    }

    /**
     * Validate magic link token and extract driver ID
     * 
     * @param token Token from URL parameter
     * @return Driver ID if valid, null if expired/invalid
     */
    public String validateToken(String token) {
        MagicLinkToken linkToken = tokenStore.get(token);

        if (linkToken == null) {
            log.warn("Magic link token {} not found", token);
            return null;
        }

        if (linkToken.expiresAt.isBefore(Instant.now())) {
            log.warn("Magic link token {} expired at {}", token, linkToken.expiresAt);
            tokenStore.remove(token);
            return null;
        }

        return linkToken.driverId;
    }

    /**
     * Track assignment status of a specific route
     * 
     * @param routeId Optimization solution ID
     * @return Assignment status summary
     */
    public RouteAssignmentStatus trackAssignmentStatus(UUID routeId) {
        return solutionRepository.findById(routeId)
                .map(solution -> {
                    OptimizationSolutionEntity.Status status = solution.getStatus();
                    int totalRoutes = solution.getTotalRoutes() != null ? solution.getTotalRoutes() : 0;

                    return new RouteAssignmentStatus(
                            routeId,
                            status.toString(),
                            totalRoutes,
                            solution.getCreatedAt(),
                            List.of() // TODO: Extract assigned drivers from routes
                    );
                })
                .orElse(null);
    }

    private void sendNotification(String contact, String driverId, String magicLinkUrl) {
        // TODO: Integrate with SMS/Email provider (Twilio, SendGrid, etc.)
        log.info("NOTIFICATION: Sending magic link to {} for driver {}: {}",
                contact, driverId, magicLinkUrl);

        // Example: If email
        if (contact.contains("@")) {
            // emailService.send(contact, "Your Route Assignment", "Click here: " +
            // magicLinkUrl);
        } else {
            // smsService.send(contact, "Your route is ready: " + magicLinkUrl);
        }
    }

    // Data classes
    private record MagicLinkToken(String driverId, UUID routeId, Instant expiresAt) {
    }

    public record RouteAssignmentStatus(
            UUID routeId,
            String status,
            int totalRoutes,
            Instant createdAt,
            List<String> assignedDrivers) {
    }
}
