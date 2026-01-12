package com.example.admin_panel_service.service;

import com.example.admin_panel_service.client.PlanningServiceClient;
import com.example.danxils_commons.dto.FleetVehicleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FleetAdminService {

    private final PlanningServiceClient planningServiceClient;

    public List<FleetVehicleDto> getAllVehicles() {
        return planningServiceClient.getAllVehicles();
    }

    public FleetVehicleDto createVehicle(FleetVehicleDto vehicleDto) {
        return planningServiceClient.createVehicle(vehicleDto);
    }

    public FleetVehicleDto getVehicleById(UUID vehicleId) {
        return planningServiceClient.getVehicleById(vehicleId);
    }

    public FleetVehicleDto updateVehicle(UUID vehicleId, FleetVehicleDto vehicleDto) {
        return planningServiceClient.updateVehicle(vehicleId, vehicleDto);
    }

    public void deleteVehicle(UUID vehicleId) {
        planningServiceClient.deleteVehicle(vehicleId);
    }
}