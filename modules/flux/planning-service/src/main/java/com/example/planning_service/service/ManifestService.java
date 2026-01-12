package com.example.planning_service.service;

import com.example.planning_service.dto.ManifestResponseDto;
import com.example.planning_service.entity.ManifestEntity;
import com.example.planning_service.entity.ManifestRouteEntity;
import com.example.planning_service.entity.ManifestStatus;
import com.example.planning_service.optimization.VrpOptimizerService;
import com.example.planning_service.repository.ManifestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enhanced Manifest Service with generation, assignment, and query
 * capabilities.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ManifestService {
    private final ManifestRepository manifestRepository;

    /**
     * Create a manifest from VRP solution.
     */
    @Transactional
    public ManifestEntity createManifestFromVehicleRoutingSolution(
            VrpOptimizerService.VehicleRoutingSolution.Route vrpRoute,
            LocalDate date) {

        log.info("Creating manifest from VRP solution for vehicle: {}", vrpRoute.vehicleId());

        ManifestEntity manifest = ManifestEntity.builder()
                .id(UUID.randomUUID())
                .vehicleId(vrpRoute.vehicleId())
                .date(date)
                .status(ManifestStatus.DRAFT)
                .totalDistanceMeters(vrpRoute.totalDistance())
                .estimatedDurationMillis(vrpRoute.totalTimeMillis())
                .optimizationScore(calculateOptimizationScore(vrpRoute))
                .externalReference("MAN-" + date.toString().replace("-", "") + "-"
                        + vrpRoute.vehicleId().toString().substring(0, 4).toUpperCase())
                .metadata("{\"ecrm_version\": \"v1.0\", \"source\": \"vrp_optimizer\"}")
                .build();

        // Create routes from VRP activities
        List<ManifestRouteEntity> routes = vrpRoute.activities().stream()
                .map(activity -> ManifestRouteEntity.builder()
                        .id(UUID.randomUUID())
                        .manifest(manifest)
                        .orderId(activity.orderId())
                        .sequence(vrpRoute.activities().indexOf(activity) + 1)
                        .address(formatAddress(activity.latitude(), activity.longitude()))
                        .timeWindow("09:00-17:00") // TODO: Get from order
                        .estimatedArrival(formatTime(activity.arrivalTimeMillis()))
                        .status("PENDING")
                        .build())
                .collect(Collectors.toList());

        manifest.setRoutes(routes);

        ManifestEntity saved = manifestRepository.save(manifest);
        log.info("Created manifest with ID: {} containing {} routes", saved.getId(), routes.size());

        return saved;
    }

    /**
     * Create a manifest from manual publish request.
     */
    @Transactional
    public ManifestEntity createManifest(
            UUID driverId,
            UUID vehicleId,
            LocalDate date,
            List<ManifestRouteEntity> routes) {

        log.info("Creating manifest for driver {} on {}", driverId, date);

        ManifestEntity manifest = ManifestEntity.builder()
                .id(UUID.randomUUID())
                .driverId(driverId)
                .vehicleId(vehicleId)
                .date(date)
                .status(ManifestStatus.ASSIGNED) // Directly assigned
                .routes(routes)
                .totalDistanceMeters(0) // TODO: Calculate or pass from request
                .estimatedDurationMillis(0)
                .externalReference("MAN-" + date.toString().replace("-", "") + "-"
                        + vehicleId.toString().substring(0, 4).toUpperCase())
                .metadata("{\"ecrm_version\": \"v1.0\", \"source\": \"manual_publish\"}")
                .build();

        // Fix bidirectional relationship
        routes.forEach(r -> {
            r.setManifest(manifest);
            if (r.getId() == null)
                r.setId(UUID.randomUUID());
            r.setStatus("PENDING");
        });

        return manifestRepository.save(manifest);
    }

    /**
     * Assign a driver to a manifest.
     */
    @Transactional
    public ManifestEntity assignDriver(UUID manifestId, UUID driverId) {
        log.info("Assigning driver {} to manifest {}", driverId, manifestId);

        ManifestEntity manifest = getManifest(manifestId);

        if (manifest.getStatus() != ManifestStatus.DRAFT) {
            throw new IllegalStateException(
                    "Cannot assign driver to manifest in status: " + manifest.getStatus());
        }

        manifest.setDriverId(driverId);
        // TODO: ideally fetch driver name here and set manifest.setDriverName(...)
        manifest.setStatus(ManifestStatus.ASSIGNED);

        return manifestRepository.save(manifest);
    }

    /**
     * Update manifest status.
     */
    @Transactional
    public ManifestEntity updateStatus(UUID manifestId, ManifestStatus newStatus) {
        log.info("Updating manifest {} status to {}", manifestId, newStatus);

        ManifestEntity manifest = getManifest(manifestId);
        validateStatusTransition(manifest.getStatus(), newStatus);

        manifest.setStatus(newStatus);
        return manifestRepository.save(manifest);
    }

    /**
     * Get manifests for a specific driver on a specific date.
     */
    public List<ManifestEntity> getManifestsForDriver(UUID driverId, LocalDate date) {
        log.debug("Fetching manifests for driver {} on {}", driverId, date);
        return manifestRepository.findByDriverIdAndDate(driverId, date);
    }

    /**
     * Get all manifests for a specific date.
     */
    public List<ManifestEntity> getManifestsByDate(LocalDate date) {
        log.debug("Fetching manifests for date {}", date);
        return manifestRepository.findByDate(date);
    }

    /**
     * Get manifests by status.
     */
    public List<ManifestEntity> getManifestsByStatus(ManifestStatus status) {
        log.debug("Fetching manifests with status {}", status);
        return manifestRepository.findByStatus(status);
    }

    /**
     * Get manifests within a date range.
     */
    public List<ManifestEntity> getManifestsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching manifests between {} and {}", startDate, endDate);
        return manifestRepository.findByDateBetween(startDate, endDate);
    }

    /**
     * Get a single manifest by ID.
     */
    public ManifestEntity getManifest(UUID id) {
        return manifestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manifest not found with ID: " + id));
    }

    /**
     * Convert ManifestEntity to ManifestResponseDto.
     */
    public ManifestResponseDto toDto(ManifestEntity entity) {
        List<ManifestResponseDto.RouteDto> routeDtos = entity.getRoutes().stream()
                .map(route -> ManifestResponseDto.RouteDto.builder()
                        .id(route.getId())
                        .orderId(route.getOrderId())
                        .sequence(route.getSequence())
                        .address(route.getAddress())
                        .timeWindow(route.getTimeWindow())
                        .estimatedArrival(route.getEstimatedArrival())
                        .status(route.getStatus())
                        .build())
                .collect(Collectors.toList());

        return ManifestResponseDto.builder()
                .id(entity.getId())
                .driverId(entity.getDriverId())
                .driverName(entity.getDriverName())
                .date(entity.getDate())
                .vehicleId(entity.getVehicleId())
                .vehicleName(entity.getVehiclePlate()) // Use plate as name fallback or dedicated field
                .status(entity.getStatus().name())
                .optimizationScore(entity.getOptimizationScore())
                .totalDistanceMeters(entity.getTotalDistanceMeters())
                .estimatedDurationMillis(entity.getEstimatedDurationMillis())
                .externalReference(entity.getExternalReference())
                .vehiclePlate(entity.getVehiclePlate())
                .metadata(entity.getMetadata())
                .routes(routeDtos)
                .build();
    }

    // Helper methods

    private double calculateOptimizationScore(VrpOptimizerService.VehicleRoutingSolution.Route route) {
        // Simple score based on distance efficiency
        // TODO: Implement more sophisticated scoring
        double baseScore = 100.0;
        double distancePenalty = route.totalDistance() / 1000.0; // Penalty per km
        return Math.max(0, baseScore - distancePenalty * 0.5);
    }

    private String formatAddress(double lat, double lon) {
        return String.format("%.6f, %.6f", lat, lon);
    }

    private String formatTime(long timeMillis) {
        // Convert milliseconds to HH:mm format
        long hours = (timeMillis / (1000 * 60 * 60)) % 24;
        long minutes = (timeMillis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private void validateStatusTransition(ManifestStatus current, ManifestStatus next) {
        // Valid transitions:
        // DRAFT -> ASSIGNED
        // ASSIGNED -> IN_PROGRESS
        // IN_PROGRESS -> COMPLETED

        boolean valid = switch (current) {
            case DRAFT -> next == ManifestStatus.ASSIGNED;
            case ASSIGNED -> next == ManifestStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == ManifestStatus.COMPLETED;
            case COMPLETED -> false; // Cannot transition from COMPLETED
        };

        if (!valid) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s", current, next));
        }
    }
}
