package com.example.tracking_service.dto;

import lombok.Data;

@Data
public class AlertRequest {
    private String type; // BREAKDOWN, ACCIDENT, TRAFFIC
    private Double latitude;
    private Double longitude;
    private String description;
}
