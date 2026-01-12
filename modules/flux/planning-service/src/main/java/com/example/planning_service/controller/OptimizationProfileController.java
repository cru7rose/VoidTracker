package com.example.planning_service.controller;

import com.example.planning_service.dto.OptimizationProfileDto;
import com.example.planning_service.dto.request.CreateOptimizationProfileRequestDto;
import com.example.planning_service.service.OptimizationProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class OptimizationProfileController {

    private final OptimizationProfileService profileService;

    @GetMapping
    public ResponseEntity<List<OptimizationProfileDto>> getAll() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OptimizationProfileDto> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(profileService.getProfile(id));
    }

    @PostMapping
    public ResponseEntity<OptimizationProfileDto> create(@RequestBody CreateOptimizationProfileRequestDto request) {
        return ResponseEntity.ok(profileService.createProfile(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OptimizationProfileDto> update(@PathVariable UUID id,
            @RequestBody CreateOptimizationProfileRequestDto request) {
        return ResponseEntity.ok(profileService.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        profileService.deleteProfile(id);
        return ResponseEntity.ok().build();
    }
}
