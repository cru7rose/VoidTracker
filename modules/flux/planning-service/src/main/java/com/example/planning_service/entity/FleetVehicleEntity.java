// File: planning-service/src/main/java/com/example/planning_service/entity/FleetVehicleEntity.java
package com.example.planning_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * ARCHITEKTURA: Encja reprezentująca pojedynczy pojazd we flocie. W ramach MVP,
 * model ten jest celowo uproszczony i zawiera jedynie podstawowe atrybuty
 * niezbędne dla algorytmu VRP, takie jak ID i pojemność.
 */
@Entity
@Table(name = "planning_fleet_vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetVehicleEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Double capacityWeight;

    @Column(nullable = false)
    private Double capacityVolume;

    @Builder.Default
    @Column(nullable = true)
    private Boolean available = true;

    @Column(name = "driver_id")
    private String driverId; // UUID or Keycloak ID

    @Column(name = "profile_id")
    private String profileId;

    @Column(name = "carrier_id")
    private String carrierId;

    // --- Elastic Shell (Milkrun) Properties ---

    @Column(name = "is_fixed_route")
    private Boolean isFixedRoute;

    /**
     * JSON definition of fixed stops (immutable skeleton)
     * Stored as String for MVP, typically a List<FixedStopDto>
     */
    @Column(name = "fixed_stops", columnDefinition = "TEXT")
    private String fixedStops;

    @Column(name = "max_detour_km")
    private Double maxDetourKm;
}