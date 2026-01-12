package com.example.planning_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicle_checks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCheckEntity {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID vehicleId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private boolean isOk;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String issuesJson; // e.g., ["Tires worn", "Check engine light"] or details object
}
