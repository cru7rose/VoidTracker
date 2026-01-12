package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating or updating a route assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteAssignmentRequestDto {

    /**
     * Reference to optimization solution (optional on create if creating from
     * scratch)
     */
    private UUID optimizationSolutionId;

    /**
     * ID of assigned vehicle (from danxils-mesh)
     */
    private UUID vehicleId;

    /**
     * ID of assigned driver (from IAM)
     */
    private UUID driverId;

    /**
     * Optional carrier ID for external carriers
     */
    private UUID carrierId;

    /**
     * Human-readable route name
     */
    private String routeName;

    /**
     * Complete route data (activities, stops, coordinates, etc.)
     * This is the parsed solution from Timefold optimizer
     */
    private Map<String, Object> routeData;

    /**
     * Optional status (defaults to DRAFT if not provided)
     */
    private String status;
}
