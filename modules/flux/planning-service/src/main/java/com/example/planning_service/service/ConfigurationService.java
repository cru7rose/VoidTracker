package com.example.planning_service.service;

import com.example.planning_service.entity.BusinessRuleEntity;
import com.example.planning_service.entity.OptimizationProfileEntity;
import com.example.planning_service.entity.ViewConfigurationEntity;
import com.example.planning_service.repository.BusinessRuleRepository;
import com.example.planning_service.repository.OptimizationProfileRepository;
import com.example.planning_service.repository.ViewConfigurationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private final OptimizationProfileRepository profileRepository;
    private final BusinessRuleRepository ruleRepository;
    private final ViewConfigurationRepository viewRepository;

    @PostConstruct
    public void initDefaults() {
        // Initialize default profiles if empty
        if (profileRepository.count() == 0) {
            profileRepository.save(OptimizationProfileEntity.builder()
                    .code("PUDO_CLUSTER")
                    .name("PUDO Cluster (Locker Focus)")
                    .baseAlgorithm("CLUSTERING")
                    .constraintsJson("{\"focus\": \"LOCKER\", \"capacityCheck\": true}")
                    .build());
            profileRepository.save(OptimizationProfileEntity.builder()
                    .code("DYNAMIC_SIMULATION")
                    .name("Dynamic Simulation (Digital Twin)")
                    .baseAlgorithm("SIMULATION")
                    .constraintsJson("{\"realtime\": true, \"simulationDepth\": 5}")
                    .build());
            profileRepository.save(OptimizationProfileEntity.builder()
                    .code("STRICT_CONSTRAINT")
                    .name("Strict Constraint (AETR)")
                    .baseAlgorithm("CONSTRAINT_SOLVER")
                    .constraintsJson("{\"aetr\": true, \"strictTimeWindows\": true}")
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public Optional<OptimizationProfileEntity> getProfile(String code) {
        return profileRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public List<OptimizationProfileEntity> getAllProfiles() {
        return profileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<BusinessRuleEntity> getAllRules() {
        return ruleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ViewConfigurationEntity> getViewConfig(String viewId) {
        return viewRepository.findByViewId(viewId);
    }
}
