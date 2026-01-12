package com.example.planning_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResequenceRequest {
    private Double currentLat;
    private Double currentLng;
    private List<RouteStopDto> stops;
}
