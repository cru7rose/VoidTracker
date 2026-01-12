package com.example.planning_service.service;

import com.example.danxils_commons.dto.FleetVehicleDto;
import com.example.planning_service.entity.FleetVehicleEntity;
import com.example.planning_service.mapper.FleetVehicleMapper;
import com.example.planning_service.repository.FleetVehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Implementacja serwisu do zarządzania flotą.
 * Hermetyzuje logikę biznesową operacji CRUD, włączając walidację,
 * transformację danych i interakcję z warstwą repozytorium.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FleetAdminService {

    private final FleetVehicleRepository vehicleRepository;
    private final FleetVehicleMapper vehicleMapper;

    @Transactional
    public FleetVehicleDto createVehicle(FleetVehicleDto vehicleDto) {
        log.info("Creating new vehicle with name: '{}'", vehicleDto.name());
        FleetVehicleEntity entity = vehicleMapper.toEntity(vehicleDto);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        FleetVehicleEntity savedEntity = vehicleRepository.save(entity);
        log.info("Successfully created vehicle with ID: {}", savedEntity.getId());
        return vehicleMapper.toDto(savedEntity);
    }

    @Transactional(readOnly = true)
    public FleetVehicleDto getVehicleById(UUID vehicleId) {
        log.debug("Fetching vehicle with ID: {}", vehicleId);
        return vehicleRepository.findById(vehicleId)
                .map(vehicleMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with ID: " + vehicleId));
    }

    @Transactional(readOnly = true)
    public List<FleetVehicleDto> getAllVehicles() {
        log.debug("Fetching all vehicles from the fleet");
        return vehicleRepository.findAll().stream()
                .map(vehicleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FleetVehicleDto updateVehicle(UUID vehicleId, FleetVehicleDto vehicleDto) {
        log.info("Updating vehicle with ID: {}", vehicleId);
        FleetVehicleEntity existingEntity = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with ID: " + vehicleId));

        existingEntity.setName(vehicleDto.name());
        existingEntity.setCapacityWeight(vehicleDto.capacityWeight());
        existingEntity.setCapacityVolume(vehicleDto.capacityVolume());

        FleetVehicleEntity savedEntity = vehicleRepository.save(existingEntity);
        log.info("Successfully updated vehicle with ID: {}", savedEntity.getId());
        return vehicleMapper.toDto(savedEntity);
    }

    @Transactional
    public void deleteVehicle(UUID vehicleId) {
        log.info("Deleting vehicle with ID: {}", vehicleId);
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new EntityNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        vehicleRepository.deleteById(vehicleId);
        log.info("Successfully deleted vehicle with ID: {}", vehicleId);
    }
}