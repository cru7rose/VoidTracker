package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO containing comprehensive route information for driver overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDetailsDTO {

    private String routeId;
    private String driverId;
    private String status; // PENDING, IN_PROGRESS, COMPLETED
    private LocalDate date;

    // Summary statistics
    private int totalStops;
    private int completedStops;
    private double totalDistanceKm;
    private String estimatedDuration; // formatted as "2h 30min"

    // List of stops with coordinates
    private List<RouteStopDto> stops;
}
