package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "planning_manifest_routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestRouteEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "manifest_id")
    private ManifestEntity manifest;

    private UUID orderId;

    private int sequence;

    private String address;

    private String timeWindow;

    private String estimatedArrival;

    private String status; // PENDING, COMPLETED
}
