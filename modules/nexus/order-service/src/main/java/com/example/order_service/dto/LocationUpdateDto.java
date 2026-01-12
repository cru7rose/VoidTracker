package com.example.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateDto {
    private String driverId;
    private Double lat;
    private Double lng;
    private Instant timestamp;
}
