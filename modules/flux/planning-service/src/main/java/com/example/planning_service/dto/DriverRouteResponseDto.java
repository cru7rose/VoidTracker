package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for driver route response (used by Ghost PWA).
 * Contains route data optimized for mobile app consumption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRouteResponseDto {
    
    private UUID routeId;
    private String routeName;
    private UUID driverId;
    private UUID vehicleId;
    private UUID carrierId;
    private String status;
    
    /**
     * Complete route data (stops, activities, coordinates)
     * This is the JSONB data from route_data column
     */
    private Map<String, Object> routeData;
    
    /**
     * Computed fields for quick display
     */
    private Integer stopCount;
    private Double totalDistanceKm;
    private Long estimatedDurationMinutes;
    
    /**
     * Timestamps
     */
    private Instant createdAt;
    private Instant updatedAt;
}
