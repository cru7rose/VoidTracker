package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "optimization_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationProfileEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code; // e.g., "WARSAW_RUSH"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String baseAlgorithm; // e.g., "CVRPTW_HEURISTIC"

    @Column(columnDefinition = "TEXT")
    private String constraintsJson; // JSON configuration for enabled constraints/weights

    @Column(name = "solver_config_xml", columnDefinition = "TEXT")
    private String solverConfigXml; // Full XML config override (power user)

    // Simplified config fields
    private Integer terminationSeconds;
    private String constructionHeuristicType; // e.g., FIRST_FIT, CHEAPEST_INSERTION
    private String localSearchType; // e.g., TABU_SEARCH, HILL_CLIMBING

    // Aleet-Parity Fields
    private String depotId; // UUID or specific identifier
    private Integer maxRouteDurationMinutes; // e.g., 480 for 8 hours
    private String workStartTime; // e.g., "08:00"

    @Column(name = "vehicle_mode")
    private String vehicleSelectionMode; // "ALL", "ACTIVE", "DEPOT_SPECIFIC"
}
