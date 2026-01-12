package com.example.driver_app_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanRequestDto {
    private String barcode;
    private Double lat;
    private Double lon;
    private Instant timestamp;
    private boolean override;
    private String driverId; // Can be extracted from JWT in controller
}
