package com.example.planning_service.controller;

import com.example.planning_service.entity.FleetVehicleEntity;
import com.example.planning_service.entity.VehicleCheckEntity;
import com.example.planning_service.service.VehicleCheckService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fleet/checks")
@RequiredArgsConstructor
public class VehicleCheckController {

    private final VehicleCheckService service;

    @GetMapping("/vehicles")
    public ResponseEntity<List<FleetVehicleEntity>> getVehicles() {
        return ResponseEntity.ok(service.getAvailableVehicles());
    }

    @PostMapping
    public ResponseEntity<VehicleCheckEntity> submitCheck(@RequestBody SubmitCheckRequest request) {
        VehicleCheckEntity saved = service.submitCheck(
                request.getUserId(),
                request.getVehicleId(),
                request.isOk(),
                request.getIssuesJson());
        return ResponseEntity.ok(saved);
    }

    @Data
    public static class SubmitCheckRequest {
        private UUID userId;
        private UUID vehicleId;
        private boolean isOk;
        private String issuesJson;
    }
}
