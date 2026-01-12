package com.example.planning_service.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class OptimizationProfileDto {
    private UUID id;
    private String name;
    private String code;
    private String depotId;
    private Integer maxRouteDurationMinutes;
    private String workStartTime;
    private String vehicleSelectionMode;
}
