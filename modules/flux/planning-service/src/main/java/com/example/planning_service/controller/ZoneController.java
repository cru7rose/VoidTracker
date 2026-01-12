package com.example.planning_service.controller;

import com.example.planning_service.entity.ZoneDefinitionEntity;
import com.example.planning_service.service.ZoneResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneResolutionService zoneService;

    @GetMapping("/resolve")
    public ResponseEntity<ZoneDefinitionEntity> resolveZone(
            @RequestParam String country,
            @RequestParam String code) {
        
        return zoneService.resolveZone(country, code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
