package com.example.planning_service.service;

import com.example.planning_service.client.IamClient;
import com.example.planning_service.dto.DriverRouteResponseDto;
import com.example.planning_service.dto.RouteAssignmentFilterDto;
import com.example.planning_service.dto.RouteAssignmentRequestDto;
import com.example.planning_service.dto.RouteAssignmentResponseDto;
import com.example.planning_service.dto.UserInfoDto;
import com.example.planning_service.entity.OptimizationSolutionEntity;
import com.example.planning_service.entity.RouteAssignmentEntity;
import com.example.planning_service.entity.CarrierComplianceEntity;
import com.example.planning_service.repository.CarrierComplianceRepository;
import com.example.planning_service.repository.OptimizationSolutionRepository;
import com.example.planning_service.repository.RouteAssignmentRepository;
import com.example.planning_service.repository.specification.RouteAssignmentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

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
    private final RouteAssignmentSpecification specification;
    private final CarrierComplianceRepository carrierComplianceRepository;
    private final IamClient iamClient;
    private final EmailService emailService;
    private final Map<String, MagicLinkToken> tokenStore = new HashMap<>();
    
    // Cache for driver/vehicle names (5min TTL)
    private final Map<UUID, CachedUserInfo> driverCache = new HashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    
    private record CachedUserInfo(String name, long timestamp) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

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
     * Batch save multiple route assignments (optimized for high volume).
     * Uses bulk insert for better performance with thousands of orders.
     * 
     * @param dtos List of route assignment DTOs
     * @return List of created assignment IDs
     */
    @Transactional
    public List<UUID> saveBatchRouteAssignments(List<RouteAssignmentRequestDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        // Convert DTOs to entities
        List<RouteAssignmentEntity> entities = dtos.stream()
                .map(dto -> RouteAssignmentEntity.builder()
                        .optimizationSolutionId(dto.getOptimizationSolutionId())
                        .vehicleId(dto.getVehicleId())
                        .driverId(dto.getDriverId())
                        .carrierId(dto.getCarrierId())
                        .routeName(dto.getRouteName())
                        .routeData(dto.getRouteData())
                        .status(parseStatus(dto.getStatus()))
                        .build())
                .collect(Collectors.toList());

        // Bulk save (JPA will batch inserts if configured)
        List<RouteAssignmentEntity> saved = routeAssignmentRepository.saveAll(entities);
        
        List<UUID> ids = saved.stream()
                .map(RouteAssignmentEntity::getId)
                .collect(Collectors.toList());

        log.info("Batch created {} route assignments", ids.size());
        return ids;
    }

    /**
     * Get all route assignments with enriched data (DEPRECATED - use findFiltered instead)
     * This method loads ALL assignments into memory - not suitable for production!
     */
    @Deprecated
    public List<RouteAssignmentResponseDto> getAllRouteAssignments() {
        return routeAssignmentRepository.findAll()
                .stream()
                .map(this::enrichResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered and paginated route assignments.
     * Designed for high-volume scenarios (thousands of orders per day).
     * 
     * @param filter Filter criteria (status, driver, dates, etc.)
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of route assignments with enriched data
     */
    public Page<RouteAssignmentResponseDto> findFiltered(RouteAssignmentFilterDto filter, Pageable pageable) {
        Specification<RouteAssignmentEntity> spec = specification.filterBy(filter);
        Page<RouteAssignmentEntity> entityPage = routeAssignmentRepository.findAll(spec, pageable);
        return entityPage.map(this::enrichResponse);
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
     * 
     * @param assignmentId Route assignment ID
     * @param driverEmail Optional driver email (if null, will try to fetch from IAM or log warning)
     * @return Magic link URL
     */
    @Transactional
    public String publishRouteToDriver(UUID assignmentId, String driverEmail) {
        RouteAssignmentEntity assignment = routeAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Route assignment not found: " + assignmentId));

        if (assignment.getDriverId() == null) {
            throw new IllegalStateException("Cannot publish route without assigned driver");
        }

        // Validate carrier compliance before publishing
        if (assignment.getCarrierId() != null) {
            validateCarrierCompliance(assignment.getCarrierId());
        }

        // Generate magic link
        String driverIdStr = assignment.getDriverId().toString();
        
        // Try to get driver email from IAM if not provided
        String contact = driverEmail;
        if (contact == null || contact.isBlank()) {
            contact = getDriverEmail(assignment.getDriverId());
        }
        
        // Fallback placeholder if still no email
        if (contact == null || contact.isBlank()) {
            contact = "driver-" + driverIdStr + "@voidtracker.app"; // Placeholder for logging
            log.warn("No email found for driver {}, using placeholder", driverIdStr);
        }
        
        String magicLink = generateMagicLink(driverIdStr, contact, assignmentId);

        // Update status to PUBLISHED
        assignment.setStatus(RouteAssignmentEntity.RouteStatus.PUBLISHED);
        routeAssignmentRepository.save(assignment);

        log.info("Published route assignment {} to driver {}", assignmentId, driverIdStr);
        return magicLink;
    }

    /**
     * Validate carrier compliance before publishing route
     * 
     * @param carrierId Carrier UUID
     * @throws IllegalStateException if carrier is not compliant
     */
    private void validateCarrierCompliance(UUID carrierId) {
        CarrierComplianceEntity compliance = carrierComplianceRepository.findById(carrierId.toString())
                .orElse(null);

        if (compliance == null) {
            log.warn("Carrier {} has no compliance record. Allowing publish but should be reviewed.", carrierId);
            return; // Allow publish but log warning
        }

        // Check compliance status
        if (!"COMPLIANT".equals(compliance.getComplianceStatus())) {
            throw new IllegalStateException(
                    String.format("Cannot publish route: Carrier %s is %s. Status must be COMPLIANT.",
                            carrierId, compliance.getComplianceStatus()));
        }

        // Check insurance
        if (!Boolean.TRUE.equals(compliance.getIsInsured())) {
            throw new IllegalStateException(
                    String.format("Cannot publish route: Carrier %s is not insured.", carrierId));
        }

        // Check insurance expiry (if set)
        if (compliance.getInsuranceExpiryDate() != null) {
            if (compliance.getInsuranceExpiryDate().isBefore(java.time.LocalDate.now())) {
                throw new IllegalStateException(
                        String.format("Cannot publish route: Carrier %s insurance expired on %s.",
                                carrierId, compliance.getInsuranceExpiryDate()));
            }

            // Warn if expiring soon (within 30 days)
            if (compliance.getInsuranceExpiryDate().isBefore(java.time.LocalDate.now().plusDays(30))) {
                log.warn("Carrier {} insurance expires soon: {}", carrierId, compliance.getInsuranceExpiryDate());
            }
        }

        log.debug("Carrier {} compliance validated successfully", carrierId);
    }

    /**
     * Publish route to driver (overload without email - for backward compatibility)
     */
    @Transactional
    public String publishRouteToDriver(UUID assignmentId) {
        return publishRouteToDriver(assignmentId, null);
    }

    // ========== Helper Methods ==========

    /**
     * Enrich response DTO with external data (driver/vehicle names)
     * Fetches real data from IAM service with caching for performance
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

        // Enrich with vehicle name (TODO: fetch from danxils-mesh/Titan)
        if (entity.getVehicleId() != null) {
            dto.setVehicleName("Vehicle " + entity.getVehicleId().toString().substring(0, 8));
        }
        
        // Enrich with driver name from IAM service
        if (entity.getDriverId() != null) {
            String driverName = getDriverName(entity.getDriverId());
            dto.setDriverName(driverName);
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

    /**
     * Get driver name from IAM service with caching
     * 
     * @param driverId Driver UUID
     * @return Driver name (username or fullName) or fallback
     */
    private String getDriverName(UUID driverId) {
        // Check cache first
        CachedUserInfo cached = driverCache.get(driverId);
        if (cached != null && !cached.isExpired()) {
            return cached.name();
        }

        // Fetch from IAM service
        try {
            UserInfoDto user = iamClient.getUserById(driverId);
            if (user != null) {
                String name = user.getFullName() != null && !user.getFullName().isBlank()
                        ? user.getFullName()
                        : (user.getUsername() != null ? user.getUsername() : "Driver " + driverId.toString().substring(0, 8));
                
                // Update cache
                driverCache.put(driverId, new CachedUserInfo(name, System.currentTimeMillis()));
                return name;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch driver name for {}: {}", driverId, e.getMessage());
        }

        // Fallback
        return "Driver " + driverId.toString().substring(0, 8);
    }

    /**
     * Get driver email from IAM service (for magic link sending)
     * 
     * @param driverId Driver UUID
     * @return Driver email or null if not found
     */
    public String getDriverEmail(UUID driverId) {
        try {
            UserInfoDto user = iamClient.getUserById(driverId);
            return user != null ? user.getEmail() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch driver email for {}: {}", driverId, e.getMessage());
            return null;
        }
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
     * Get route ID from token (for Ghost PWA authentication)
     * 
     * @param token Magic link token
     * @return Route assignment ID if token is valid, null otherwise
     */
    public UUID getRouteIdFromToken(String token) {
        MagicLinkToken linkToken = tokenStore.get(token);
        if (linkToken == null || linkToken.expiresAt.isBefore(Instant.now())) {
            return null;
        }
        return linkToken.routeId;
    }

    /**
     * Get active route for driver (PUBLISHED or IN_PROGRESS status)
     * Used by Ghost PWA to display route after authentication.
     * 
     * @param driverId Driver UUID from IAM
     * @return DriverRouteResponseDto with route data, or null if no active route
     */
    public DriverRouteResponseDto getActiveRouteForDriver(UUID driverId) {
        List<RouteAssignmentEntity> activeRoutes = routeAssignmentRepository.findByDriverIdAndStatus(
                driverId, 
                RouteAssignmentEntity.RouteStatus.PUBLISHED
        );
        
        // Also check IN_PROGRESS routes
        List<RouteAssignmentEntity> inProgressRoutes = routeAssignmentRepository.findByDriverIdAndStatus(
                driverId,
                RouteAssignmentEntity.RouteStatus.IN_PROGRESS
        );
        
        // Combine and get most recent
        List<RouteAssignmentEntity> allActive = new ArrayList<>();
        allActive.addAll(activeRoutes);
        allActive.addAll(inProgressRoutes);
        
        if (allActive.isEmpty()) {
            log.info("No active route found for driver {}", driverId);
            return null;
        }
        
        // Get most recent route (by createdAt)
        RouteAssignmentEntity route = allActive.stream()
                .max(Comparator.comparing(RouteAssignmentEntity::getCreatedAt))
                .orElse(null);
        
        if (route == null) {
            return null;
        }
        
        // Convert to DTO
        return convertToDriverRouteDto(route);
    }

    /**
     * Convert RouteAssignmentEntity to DriverRouteResponseDto
     */
    private DriverRouteResponseDto convertToDriverRouteDto(RouteAssignmentEntity entity) {
        DriverRouteResponseDto.DriverRouteResponseDtoBuilder builder = DriverRouteResponseDto.builder()
                .routeId(entity.getId())
                .routeName(entity.getRouteName())
                .driverId(entity.getDriverId())
                .vehicleId(entity.getVehicleId())
                .carrierId(entity.getCarrierId())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .routeData(entity.getRouteData())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());
        
        // Extract computed fields from routeData
        if (entity.getRouteData() != null) {
            Object activities = entity.getRouteData().get("activities");
            if (activities instanceof List) {
                builder.stopCount(((List<?>) activities).size());
            }
            Object distance = entity.getRouteData().get("totalDistance");
            if (distance instanceof Number) {
                builder.totalDistanceKm(((Number) distance).doubleValue() / 1000.0);
            }
            Object duration = entity.getRouteData().get("totalDuration");
            if (duration instanceof Number) {
                builder.estimatedDurationMinutes(((Number) duration).longValue() / 60);
            }
        }
        
        return builder.build();
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

    /**
     * Send notification (email or SMS) with magic link
     * 
     * @param contact Email address or phone number
     * @param driverId Driver ID (for logging)
     * @param magicLinkUrl Full magic link URL
     */
    private void sendNotification(String contact, String driverId, String magicLinkUrl) {
        log.info("Sending magic link notification to {} for driver {}: {}",
                contact, driverId, magicLinkUrl);

        // Check if contact is email or phone
        if (contact != null && contact.contains("@")) {
            // Send email
            try {
                emailService.sendMagicLink(contact, magicLinkUrl, null);
                log.info("Successfully sent magic link email to {}", contact);
            } catch (Exception e) {
                log.error("Failed to send email to {}, will log for manual sending", contact, e);
                // Don't throw - allow route to be published even if email fails
                // In production, could queue for retry or use SMS fallback
            }
        } else {
            // TODO: Implement SMS service (Twilio, AWS SNS, etc.)
            log.warn("SMS notification not yet implemented. Phone: {}", contact);
            log.info("MANUAL ACTION REQUIRED: Send magic link to {}: {}", contact, magicLinkUrl);
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
