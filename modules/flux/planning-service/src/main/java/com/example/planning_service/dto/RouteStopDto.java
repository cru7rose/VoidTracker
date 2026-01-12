package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopDto {
    private String stopId;
    private String type;
    private double lat;
    private double lon;
    private UUID orderId;
    private Instant plannedArrival;

    // Additional fields for driver app if needed, based on usage in DriverService
    // DriverService uses: sequence, address, timeWindow, estimatedArrival, status,
    // customerName, packageCount
    // I should check if I need to add these.
    // The previous file (Step 1847) only had: stopId, type, lat, lon, orderId,
    // plannedArrival.
    // But DriverService (Step 1870) uses builder().sequence(...).address(...).
    // This implies DriverService expects MORE fields.
    // I need to add these fields to support DriverService.

    private Integer sequence;
    private String address;
    private String timeWindow;
    private String estimatedArrival;
    private String status;
    private String customerName;
    private Integer packageCount;
}
