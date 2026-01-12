package com.example.planning_service.controller;

import com.example.danxils_commons.dto.FleetVehicleDto;
import com.example.planning_service.service.FleetAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Nowy, zabezpieczony kontroler REST do administracyjnego
 * zarządzania flotą pojazdów. Udostępnia pełen zestaw endpointów CRUD,
 * które będą wywoływane przez `admin-panel-service`.
 */
@RestController
@RequestMapping("/api/admin/fleet/vehicles")
@RequiredArgsConstructor
public class FleetAdminController {

    private final FleetAdminService fleetAdminService;

    @PostMapping
    public ResponseEntity<FleetVehicleDto> createVehicle(@RequestBody FleetVehicleDto vehicleDto) {
        FleetVehicleDto createdVehicle = fleetAdminService.createVehicle(vehicleDto);
        return new ResponseEntity<>(createdVehicle, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FleetVehicleDto>> getAllVehicles() {
        return ResponseEntity.ok(fleetAdminService.getAllVehicles());
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<FleetVehicleDto> getVehicleById(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(fleetAdminService.getVehicleById(vehicleId));
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<FleetVehicleDto> updateVehicle(@PathVariable UUID vehicleId, @RequestBody FleetVehicleDto vehicleDto) {
        return ResponseEntity.ok(fleetAdminService.updateVehicle(vehicleId, vehicleDto));
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable UUID vehicleId) {
        fleetAdminService.deleteVehicle(vehicleId);
        return ResponseEntity.noContent().build();
    }
}