package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "planning_manifests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestEntity {

    @Id
    private UUID id;

    private UUID driverId;

    private LocalDate date;

    private UUID vehicleId;

    @Enumerated(EnumType.STRING)
    private ManifestStatus status;

    private double optimizationScore;

    private double totalDistanceMeters;

    private long estimatedDurationMillis;

    // Modern Manifest Fields (eCRM Compatibility)
    private String externalReference;

    private String driverName; // Snapshot

    private String vehiclePlate; // Snapshot

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String metadata;

    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ManifestRouteEntity> routes;
}
