// File: planning-service/src/main/java/com/example/planning_service/service/RoutePlanServiceImpl.java
package com.example.planning_service.service;

import com.example.planning_service.dto.RoutePlanDto;
import com.example.planning_service.entity.RoutePlanEntity;
import com.example.planning_service.mapper.RoutePlanMapper;
import com.example.planning_service.repository.RoutePlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Implementacja serwisu do zarządzania szablonami planów tras (RoutePlan).
 * Hermetyzuje całą logikę biznesową operacji CRUD, włączając w to walidację,
 * transformację danych (przy użyciu mappera) i interakcję z warstwą repozytorium.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoutePlanServiceImpl implements RoutePlanService {

    private final RoutePlanRepository routePlanRepository;
    private final RoutePlanMapper routePlanMapper;

    @Override
    @Transactional
    public RoutePlanDto createPlan(RoutePlanDto routePlanDto) {
        log.info("Creating a new route plan with name: '{}'", routePlanDto.name());
        RoutePlanEntity entity = routePlanMapper.toEntity(routePlanDto);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        RoutePlanEntity savedEntity = routePlanRepository.save(entity);
        log.info("Successfully created route plan with ID: {}", savedEntity.getId());
        return routePlanMapper.toDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public RoutePlanDto getPlanById(UUID planId) {
        log.debug("Fetching route plan with ID: {}", planId);
        return routePlanRepository.findById(planId)
                .map(routePlanMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Route plan not found with ID: " + planId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutePlanDto> getAllPlans() {
        log.debug("Fetching all route plans");
        return routePlanRepository.findAll().stream()
                .map(routePlanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoutePlanDto updatePlan(UUID planId, RoutePlanDto routePlanDto) {
        log.info("Updating route plan with ID: {}", planId);
        RoutePlanEntity existingEntity = routePlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Route plan not found with ID: " + planId));

        // Map DTO to a new entity, then update fields of the existing one
        RoutePlanEntity updatedData = routePlanMapper.toEntity(routePlanDto);
        existingEntity.setName(updatedData.getName());
        existingEntity.setTriggerType(updatedData.getTriggerType());
        existingEntity.setCronExpression(updatedData.getCronExpression());
        existingEntity.setFilters(updatedData.getFilters());

        RoutePlanEntity savedEntity = routePlanRepository.save(existingEntity);
        log.info("Successfully updated route plan with ID: {}", savedEntity.getId());
        return routePlanMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    public void deletePlan(UUID planId) {
        log.info("Deleting route plan with ID: {}", planId);
        if (!routePlanRepository.existsById(planId)) {
            throw new EntityNotFoundException("Route plan not found with ID: " + planId);
        }
        routePlanRepository.deleteById(planId);
        log.info("Successfully deleted route plan with ID: {}", planId);
    }
}