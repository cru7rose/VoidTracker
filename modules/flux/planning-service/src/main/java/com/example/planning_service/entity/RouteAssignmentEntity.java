package com.example.planning_service.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent Route Assignment entity.
 * Stores optimized routes with assigned driver, vehicle, and carrier.
 * Route data is stored in JSONB for flexibility.
 */
@Entity
@Table(name = "route_assignment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Reference to the optimization solution that generated this route
     */
    @Column(name = "optimization_solution_id")
    private UUID optimizationSolutionId;

    /**
     * Assigned vehicle ID (from danxils-mesh)
     */
    @Column(name = "vehicle_id")
    private UUID vehicleId;

    /**
     * Assigned driver ID (from IAM service)
     */
    @Column(name = "driver_id")
    private UUID driverId;

    /**
     * Optional carrier ID (for external carriers)
     */
    @Column(name = "carrier_id")
    private UUID carrierId;

    /**
     * Human-readable route name (e.g. "Route 1", "Warsaw North Loop")
     */
    @Column(name = "route_name", nullable = false)
    private String routeName;

    /**
     * Complete route data from optimizer (activities, stops, coordinates)
     * Stored as JSONB for flexibility and queryability
     */
    @Type(JsonType.class)
    @Column(name = "route_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> routeData;

    /**
     * Current status of the route assignment
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RouteStatus status = RouteStatus.DRAFT;

    /**
     * Audit: When this assignment was created
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Audit: When this assignment was last modified
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Audit: Who created this assignment (user ID or system)
     */
    @Column(name = "created_by")
    private String createdBy;

    /**
     * Audit: Who last modified this assignment
     */
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version")
    private Integer version;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = RouteStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Route assignment lifecycle status
     */
    public enum RouteStatus {
        /**
         * Created but not yet assigned to driver/vehicle
         */
        DRAFT,

        /**
         * Driver and vehicle have been assigned
         */
        ASSIGNED,

        /**
         * Magic link sent to driver, route published to Ghost PWA
         */
        PUBLISHED,

        /**
         * Driver has started executing the route
         */
        IN_PROGRESS,

        /**
         * All deliveries completed
         */
        COMPLETED,

        /**
         * Route cancelled or invalidated
         */
        CANCELLED
    }
}
