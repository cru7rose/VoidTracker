package com.example.planning_service.entity;

public enum ManifestStatus {
    DRAFT, // Newly generated, not yet assigned
    ASSIGNED, // Assigned to driver
    IN_PROGRESS, // Driver has started the route
    COMPLETED // All deliveries completed
}
