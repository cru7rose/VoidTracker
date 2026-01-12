package com.example.planning_service.controller;

import com.example.planning_service.entity.BusinessRuleEntity;
import com.example.planning_service.entity.OptimizationProfileEntity;
import com.example.planning_service.entity.ViewConfigurationEntity;
import com.example.planning_service.repository.BusinessRuleRepository;
import com.example.planning_service.repository.OptimizationProfileRepository;
import com.example.planning_service.repository.ViewConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigurationController {

    private final OptimizationProfileRepository profileRepository;
    private final BusinessRuleRepository ruleRepository;
    private final ViewConfigurationRepository viewRepository;

    // --- Profiles ---
    @GetMapping("/profiles")
    public List<OptimizationProfileEntity> getProfiles() {
        return profileRepository.findAll();
    }

    @PostMapping("/profiles")
    public OptimizationProfileEntity saveProfile(@RequestBody OptimizationProfileEntity profile) {
        return profileRepository.save(profile);
    }

    // --- Rules ---
    @GetMapping("/rules")
    public List<BusinessRuleEntity> getRules() {
        return ruleRepository.findAll();
    }

    @PostMapping("/rules")
    public BusinessRuleEntity saveRule(@RequestBody BusinessRuleEntity rule) {
        return ruleRepository.save(rule);
    }

    // --- Views ---
    @GetMapping("/views")
    public List<ViewConfigurationEntity> getViews() {
        return viewRepository.findAll();
    }

    @PostMapping("/views")
    public ViewConfigurationEntity saveView(@RequestBody ViewConfigurationEntity view) {
        return viewRepository.save(view);
    }
}
