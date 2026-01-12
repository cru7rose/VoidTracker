package com.example.planning_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "planning_vehicle_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleProfileEntity {

    @Id
    @Column(nullable = false)
    private String id; // e.g. "VAN_LARGE", "TRUCK_SMALL"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double maxCapacityWeight;

    @Column(nullable = false)
    private Double maxCapacityVolume;

    @Builder.Default
    @ElementCollection
    private Set<String> capabilities = new HashSet<>();
}
