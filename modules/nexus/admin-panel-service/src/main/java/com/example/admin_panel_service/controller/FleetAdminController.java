package com.example.admin_panel_service.controller;

import com.example.admin_panel_service.service.FleetAdminService;
import com.example.danxils_commons.dto.FleetVehicleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/fleet/vehicles")
@RequiredArgsConstructor
public class FleetAdminController {

    private final FleetAdminService fleetAdminService;

    @GetMapping
    public ResponseEntity<List<FleetVehicleDto>> getAllVehicles() {
        return ResponseEntity.ok(fleetAdminService.getAllVehicles());
    }

    @PostMapping
    public ResponseEntity<FleetVehicleDto> createVehicle(@RequestBody FleetVehicleDto vehicleDto) {
        FleetVehicleDto createdVehicle = fleetAdminService.createVehicle(vehicleDto);
        return new ResponseEntity<>(createdVehicle, HttpStatus.CREATED);
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<FleetVehicleDto> getVehicleById(@PathVariable UUID vehicleId) {
        return ResponseEntity.ok(fleetAdminService.getVehicleById(vehicleId));
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<FleetVehicleDto> updateVehicle(@PathVariable UUID vehicleId, @RequestBody FleetVehicleDto vehicleDto) {
        FleetVehicleDto updatedVehicle = fleetAdminService.updateVehicle(vehicleId, vehicleDto);
        return ResponseEntity.ok(updatedVehicle);
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable UUID vehicleId) {
        fleetAdminService.deleteVehicle(vehicleId);
        return ResponseEntity.noContent().build();
    }
}