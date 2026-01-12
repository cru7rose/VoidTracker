package com.example.planning_service.service;

import com.example.planning_service.entity.VehicleCheckEntity;
import com.example.planning_service.entity.FleetVehicleEntity;
import com.example.planning_service.repository.FleetVehicleRepository;
import com.example.planning_service.repository.VehicleCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleCheckService {

    private final VehicleCheckRepository repository;
    private final FleetVehicleRepository vehicleRepository;
    // In a real implementation, we would inject RestTemplate or
    // EventEmitterController to push events.

    public List<FleetVehicleEntity> getAvailableVehicles() {
        return vehicleRepository.findAll();
    }

    public VehicleCheckEntity submitCheck(UUID userId, UUID vehicleId, boolean isOk, String issuesJson) {
        VehicleCheckEntity entity = VehicleCheckEntity.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .timestamp(Instant.now())
                .isOk(isOk)
                .issuesJson(issuesJson)
                .build();

        VehicleCheckEntity saved = repository.save(entity);
        log.info("Vehicle Check Submitted: Vehicle={}, OK={}", vehicleId, isOk);

        vehicleRepository.findById(vehicleId).ifPresent(vehicle -> {
            vehicle.setAvailable(isOk);
            vehicleRepository.save(vehicle);
        });

        if (!isOk) {
            log.warn("EVENT: vehicle.breakdown_reported -> Vehicle: {}", vehicleId);
            // Here we would call EventEmitterController.emit("vehicle.breakdown_reported",
            // ...)
        }

        return saved;
    }
}
