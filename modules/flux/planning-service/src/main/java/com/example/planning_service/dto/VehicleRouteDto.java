package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRouteDto {
    private String routeId;
    private UUID vehicleId;
    private List<RouteStopDto> stops;
    private long totalDistance;
    private long totalTime;
}
