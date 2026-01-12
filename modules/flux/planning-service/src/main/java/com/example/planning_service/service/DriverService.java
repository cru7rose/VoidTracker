package com.example.planning_service.service;

import com.example.planning_service.entity.DriverSessionEntity;
import com.example.planning_service.entity.DriverWorkflowConfigEntity;
import com.example.planning_service.repository.DriverSessionRepository;
import com.example.planning_service.repository.DriverWorkflowConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverService {

    private final DriverSessionRepository sessionRepository;
    private final DriverWorkflowConfigRepository workflowRepository;
    private final NotificationService notificationService;
    private final WebhookService webhookService;
    private final com.example.danxils_commons.security.JwtUtil jwtUtil;
    private final com.example.planning_service.repository.ManifestRepository manifestRepository;

    @Transactional
    public String generateMagicLink(String driverId, String routeId, String driverEmail) {
        String token = UUID.randomUUID().toString();
        DriverSessionEntity session = DriverSessionEntity.builder()
                .driverId(driverId)
                .routeId(routeId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        sessionRepository.save(session);

        if (driverEmail != null && !driverEmail.isEmpty()) {
            notificationService.sendMagicLink(driverEmail, token);
        }

        return token;
    }

    @Transactional(readOnly = true)
    public Optional<com.example.planning_service.dto.DriverAuthResponse> validateTokenAndGenerateJWT(String token) {
        return sessionRepository.findByToken(token)
                .filter(s -> s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(session -> {
                    // Generate JWT with driver ID and DRIVER role
                    String jwt = jwtUtil.generateToken(
                            session.getDriverId(),
                            java.util.List.of("DRIVER"),
                            24 // 24 hours expiration
                    );

                    return com.example.planning_service.dto.DriverAuthResponse.builder()
                            .token(jwt)
                            .driverId(session.getDriverId())
                            .email(null) // Can be fetched from IAM if needed
                            .build();
                });
    }

    @Transactional(readOnly = true)
    public Optional<DriverWorkflowConfigEntity> getWorkflowConfig(String configCode) {
        return workflowRepository.findByConfigCode(configCode);
    }

    private final com.example.planning_service.repository.DriverTaskRepository taskRepository;

    @Transactional(readOnly = true)
    public java.util.List<com.example.planning_service.entity.DriverTaskEntity> getTasks(String driverId) {
        // Return all tasks that are not ARCHIVED (includes PENDING, COMPLETED, FAILED)
        return taskRepository.findByDriverIdAndStatusNot(driverId, "ARCHIVED");
    }

    private final com.example.planning_service.client.OrderServiceClient orderServiceClient;

    @Transactional
    public Optional<com.example.planning_service.entity.DriverTaskEntity> completeTask(UUID taskId,
            com.example.planning_service.entity.DriverTaskEntity update) {
        return taskRepository.findById(taskId).map(task -> {
            task.setStatus("COMPLETED");
            task.setCompletedAt(LocalDateTime.now());
            task.setScannedCode(update.getScannedCode());
            task.setHasPhoto(update.isHasPhoto());
            task.setHasSignature(update.isHasSignature());
            task.setPhotos(update.getPhotos()); // Save photos locally

            // Forward to order-service
            if (task.getOrderId() != null) {
                try {
                    com.example.danxils_commons.dto.request.ConfirmDeliveryRequestDto request = new com.example.danxils_commons.dto.request.ConfirmDeliveryRequestDto(
                            null, // signature handled separately or not needed here if we trust the driver app
                            update.getPhotos(),
                            task.getLat(),
                            task.getLon(),
                            "Delivered via Driver App",
                            java.util.Collections.emptyList());
                    orderServiceClient.confirmDelivery(UUID.fromString(task.getOrderId()), request);
                } catch (Exception e) {
                    log.error("Failed to forward delivery confirmation to order-service for task {}", taskId, e);
                    // We don't fail the local completion, but we log the error.
                    // ideally we should have a retry mechanism or outbox pattern here.
                }
            }

            return taskRepository.save(task);
        });
    }

    public void checkGeofence(double driverLat, double driverLon, double stopLat, double stopLon,
            double thresholdMeters) {
        double distance = calculateDistance(driverLat, driverLon, stopLat, stopLon);
        if (distance > thresholdMeters) {
            log.warn("GEOFENCE VIOLATION: Driver is {}m away from stop (threshold: {}m)", distance, thresholdMeters);
            // Log that we would trigger an external webhook here
            log.info("Would trigger n8n webhook for geofence violation");
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        double R = 6371e3; // metres
        double phi1 = lat1 * Math.PI / 180;
        double phi2 = lat2 * Math.PI / 180;
        double deltaPhi = (lat2 - lat1) * Math.PI / 180;
        double deltaLambda = (lon2 - lon1) * Math.PI / 180;

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Get driver's route overview for today
     */
    @Transactional(readOnly = true)
    public Optional<com.example.planning_service.dto.RouteDetailsDTO> getDriverRoute(String driverId) {
        UUID driverUuid = UUID.fromString(driverId);
        java.util.List<com.example.planning_service.entity.ManifestEntity> manifests = manifestRepository
                .findByDriverIdAndDate(driverUuid, java.time.LocalDate.now());

        if (manifests == null || manifests.isEmpty()) {
            return Optional.empty();
        }

        // Get the first manifest (there should only be one per driver per day)
        com.example.planning_service.entity.ManifestEntity manifest = manifests.get(0);

        // Map manifest to DTO
        // Map manifest to DTO
        java.util.List<com.example.planning_service.dto.RouteStopDto> stops = manifest.getRoutes().stream()
                .sorted(java.util.Comparator.comparingInt(
                        com.example.planning_service.entity.ManifestRouteEntity::getSequence))
                .map(route -> com.example.planning_service.dto.RouteStopDto.builder()
                        .stopId(route.getId().toString())
                        .sequence(route.getSequence())
                        .address(route.getAddress())
                        .lat(0.0) // Placeholder: Geocoding service integration required
                        .lon(0.0) // Placeholder: Geocoding service integration required
                        .timeWindow(route.getTimeWindow())
                        .estimatedArrival(route.getEstimatedArrival())
                        .status(route.getStatus().toString()) // Enum to String? ManifestStatus to String
                        .customerName(null) // TODO: Get from order service
                        .packageCount(1) // TODO: Get actual package count
                        .build())
                .collect(java.util.stream.Collectors.toList());

        long completedCount = stops.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();

        // Format duration
        String formattedDuration = formatDuration(manifest.getEstimatedDurationMillis());

        com.example.planning_service.dto.RouteDetailsDTO dto = com.example.planning_service.dto.RouteDetailsDTO
                .builder()
                .routeId(manifest.getId().toString())
                .driverId(manifest.getDriverId().toString())
                .status(manifest.getStatus().toString())
                .date(manifest.getDate())
                .totalStops(stops.size())
                .completedStops((int) completedCount)
                .totalDistanceKm(manifest.getTotalDistanceMeters() / 1000.0)
                .estimatedDuration(formattedDuration)
                .stops(stops)
                .build();

        return Optional.of(dto);
    }

    /**
     * Start the route (update status to IN_PROGRESS)
     */
    @Transactional
    public com.example.planning_service.dto.RouteDetailsDTO startRoute(String driverId) {
        UUID driverUuid = UUID.fromString(driverId);
        java.util.List<com.example.planning_service.entity.ManifestEntity> manifests = manifestRepository
                .findByDriverIdAndDate(driverUuid, java.time.LocalDate.now());

        if (manifests == null || manifests.isEmpty()) {
            throw new RuntimeException("No route found for driver today");
        }

        com.example.planning_service.entity.ManifestEntity manifest = manifests.get(0);

        manifest.setStatus(com.example.planning_service.entity.ManifestStatus.IN_PROGRESS);
        manifestRepository.save(manifest);

        return getDriverRoute(driverId)
                .orElseThrow(() -> new RuntimeException("Route not found after update"));
    }

    /**
     * Stop/End the route (update status to COMPLETED)
     */
    @Transactional
    public com.example.planning_service.dto.RouteDetailsDTO stopRoute(String driverId) {
        UUID driverUuid = UUID.fromString(driverId);
        java.util.List<com.example.planning_service.entity.ManifestEntity> manifests = manifestRepository
                .findByDriverIdAndDate(driverUuid, java.time.LocalDate.now());

        if (manifests == null || manifests.isEmpty()) {
            throw new RuntimeException("No route found for driver today");
        }

        com.example.planning_service.entity.ManifestEntity manifest = manifests.get(0);

        manifest.setStatus(com.example.planning_service.entity.ManifestStatus.COMPLETED);
        manifestRepository.save(manifest);

        return getDriverRoute(driverId)
                .orElseThrow(() -> new RuntimeException("Route not found after update"));
    }

    /**
     * Format duration in milliseconds to human-readable format
     */
    private String formatDuration(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);

        if (hours > 0) {
            return String.format("%dh %02dmin", hours, minutes);
        } else {
            return String.format("%dmin", minutes);
        }
    }
}
