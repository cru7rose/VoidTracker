package com.example.planning_service.dto;

import com.example.planning_service.entity.RouteAssignmentEntity.RouteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Filter DTO for route assignments with support for multiple criteria.
 * Designed for high-volume queries (thousands of orders per day).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteAssignmentFilterDto {
    
    /**
     * Filter by route status (DRAFT, ASSIGNED, PUBLISHED, etc.)
     */
    private List<RouteStatus> statuses;
    
    /**
     * Filter by assigned driver ID
     */
    private UUID driverId;
    
    /**
     * Filter by vehicle ID
     */
    private UUID vehicleId;
    
    /**
     * Filter by carrier ID
     */
    private UUID carrierId;
    
    /**
     * Filter by optimization solution ID
     */
    private UUID solutionId;
    
    /**
     * Filter routes created after this date (for date range queries)
     */
    private Instant createdAfter;
    
    /**
     * Filter routes created before this date
     */
    private Instant createdBefore;
    
    /**
     * Filter routes updated after this date
     */
    private Instant updatedAfter;
    
    /**
     * Filter routes updated before this date
     */
    private Instant updatedBefore;
    
    /**
     * Search by route name (partial match, case-insensitive)
     */
    private String routeNameContains;
    
    /**
     * Filter by multiple driver IDs (for bulk queries)
     */
    private List<UUID> driverIds;
    
    /**
     * Filter by multiple carrier IDs (for multi-tenant scenarios)
     */
    private List<UUID> carrierIds;
}
