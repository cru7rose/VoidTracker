package com.example.dashboard_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeatmapPointDto {
    private double lat;
    private double lon;
    private double intensity; // 0.0 to 1.0 (Profitability Score)
}
