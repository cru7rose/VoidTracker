package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "driver_workflow_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverWorkflowConfigEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String configCode; // e.g., "DEFAULT_DELIVERY", "NIGHT_DROP"

    @Column(nullable = false)
    private String name;

    // JSON configuration for the driver UI
    // Example:
    // {
    // "steps": ["SCAN_BARCODE", "TAKE_PHOTO", "SIGNATURE"],
    // "scan": { "allowManual": true, "requireMatch": true },
    // "photo": { "requireLocation": true, "minCount": 1 },
    // "geofence": { "radiusMeters": 300, "alertOnViolation": true }
    // }
    @Column(columnDefinition = "TEXT")
    private String workflowJson;
}
