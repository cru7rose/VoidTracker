package com.example.planning_service.bootstrap;

import com.example.planning_service.entity.FleetVehicleEntity;
import com.example.planning_service.repository.FleetVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final FleetVehicleRepository vehicleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initializing Planning Service Data...");

        // Create Default Fleet Vehicles (matching UI Vehicle Profiles)
        createVehicleIfNotFound("Standard Sprinter", 1200.0, 14.5);
        createVehicleIfNotFound("Box Truck (Tail-lift)", 3500.0, 35.0);
        createVehicleIfNotFound("Reefer Van", 1100.0, 11.5);

        log.info("Planning Service Data Initialization completed. Total vehicles: {}", vehicleRepository.count());
    }

    private void createVehicleIfNotFound(String name, Double capacityWeight, Double capacityVolume) {
        if (vehicleRepository.findByName(name).isEmpty()) {
            log.info("Creating fleet vehicle: {}", name);
            FleetVehicleEntity vehicle = FleetVehicleEntity.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .capacityWeight(capacityWeight)
                    .capacityVolume(capacityVolume)
                    .build();
            vehicleRepository.save(vehicle);
            log.info("Fleet vehicle {} created successfully with capacity {}kg / {}m³", name, capacityWeight,
                    capacityVolume);
        } else {
            log.debug("Fleet vehicle {} already exists, skipping creation", name);
        }
    }
}
