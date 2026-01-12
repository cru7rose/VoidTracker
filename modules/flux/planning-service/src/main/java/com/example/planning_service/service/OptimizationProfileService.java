package com.example.planning_service.service;

import com.example.planning_service.dto.OptimizationProfileDto;
import com.example.planning_service.dto.request.CreateOptimizationProfileRequestDto;
import com.example.planning_service.entity.OptimizationProfileEntity;
import com.example.planning_service.repository.OptimizationProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptimizationProfileService {

    private final OptimizationProfileRepository profileRepository;

    public List<OptimizationProfileDto> getAllProfiles() {
        return profileRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public OptimizationProfileDto getProfile(UUID id) {
        return toDto(getProfileEntity(id));
    }

    public OptimizationProfileEntity getProfileEntity(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    @Transactional
    public OptimizationProfileDto createProfile(CreateOptimizationProfileRequestDto request) {
        OptimizationProfileEntity entity = OptimizationProfileEntity.builder()
                .name(request.getName())
                .code(request.getCode() != null ? request.getCode()
                        : java.util.UUID.randomUUID().toString().substring(0, 8))
                .depotId(request.getDepotSelect())
                .maxRouteDurationMinutes(request.getMaxRouteDurationMinutes())
                .workStartTime(request.getWorkStartTime())
                .vehicleSelectionMode(request.getVehicleSelectionMode())
                .baseAlgorithm("CVRPTW") // Default
                .build();

        return toDto(profileRepository.save(entity));
    }

    @Transactional
    public OptimizationProfileDto updateProfile(UUID id, CreateOptimizationProfileRequestDto request) {
        OptimizationProfileEntity entity = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        entity.setName(request.getName());
        entity.setDepotId(request.getDepotSelect());
        entity.setMaxRouteDurationMinutes(request.getMaxRouteDurationMinutes());
        entity.setWorkStartTime(request.getWorkStartTime());
        entity.setVehicleSelectionMode(request.getVehicleSelectionMode());

        return toDto(profileRepository.save(entity));
    }

    public void deleteProfile(UUID id) {
        profileRepository.deleteById(id);
    }

    private OptimizationProfileDto toDto(OptimizationProfileEntity entity) {
        OptimizationProfileDto dto = new OptimizationProfileDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setDepotId(entity.getDepotId());
        dto.setMaxRouteDurationMinutes(entity.getMaxRouteDurationMinutes());
        dto.setWorkStartTime(entity.getWorkStartTime());
        dto.setVehicleSelectionMode(entity.getVehicleSelectionMode());
        return dto;
    }
}
