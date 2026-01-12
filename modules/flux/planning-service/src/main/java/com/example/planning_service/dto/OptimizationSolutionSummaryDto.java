package com.example.planning_service.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class OptimizationSolutionSummaryDto {
    private UUID id;
    private String name;
    private Instant createdAt;
    private String status; // DRAFT, PUBLISHED
    private Integer totalRoutes;
    private Integer totalStops;
    private Double totalDistanceMeters;
    private Long totalDurationSeconds;
    private Integer unassignedOrdersCount;
}
