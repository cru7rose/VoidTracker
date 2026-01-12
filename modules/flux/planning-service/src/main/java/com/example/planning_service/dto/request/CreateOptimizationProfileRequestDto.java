package com.example.planning_service.dto.request;

import lombok.Data;

@Data
public class CreateOptimizationProfileRequestDto {
    private String name;
    private String code;
    private String depotSelect; // "Main" or UUID
    private Integer maxRouteDurationMinutes;
    private String workStartTime; // "08:00"
    private String vehicleSelectionMode; // "ALL" or "SPECIFIC"
    // ... other config fields
}
