package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRequestDto {
    private String model; // ALEET | INPOST_STYLE | SOLVERTECH
    private String depotAlias;
    private List<UUID> vehicleIds;
    private List<UUID> orderIds;
    private UUID profileId;
    private Map<String, Object> constraints;
}
