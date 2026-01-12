package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for route assignment retrieval
 * Includes enriched data like driver/vehicle names from external services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteAssignmentResponseDto {

    private UUID id;
    private UUID optimizationSolutionId;

    // Vehicle info
    private UUID vehicleId;
    private String vehicleName; // Enriched from danxils-mesh

    // Driver info
    private UUID driverId;
    private String driverName; // Enriched from IAM service

    // Carrier info
    private UUID carrierId;
    private String carrierName; // Enriched if available

    private String routeName;
    private Map<String, Object> routeData;
    private String status;

    // Audit fields
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer version;

    // Computed fields
    private Integer stopCount; // Extracted from routeData
    private Double totalDistanceKm; // Extracted from routeData
}
