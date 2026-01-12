package com.example.planning_service.entity;

import com.example.danxils_commons.entity.BaseVoidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;

/**
 * Route Entity (Titan).
 * Represents a planned or active route.
 */
@Entity
@Table(name = "vt_route")
@Getter
@Setter
public class RouteEntity extends BaseVoidEntity {

    @Column(name = "driver_id")
    private String driverId; // Reference to IAM/Driver Service

    @Column(name = "vehicle_id")
    private String vehicleId; // Reference to Fleet Service

    @Column(columnDefinition = "geometry(LineString, 4326)")
    private LineString path; // The full route path

    @Column(name = "last_updated")
    private java.time.LocalDateTime lastUpdated;

    @jakarta.persistence.Embedded
    @jakarta.persistence.AttributeOverride(name = "coordinates", column = @Column(name = "current_coordinates"))
    private LocationPoint currentLocation;

    @Column(name = "predicted_end_time")
    private java.time.LocalDateTime predictedEndTime;

    @jakarta.persistence.OneToMany(mappedBy = "route", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private java.util.List<RouteStopEntity> stops = new java.util.ArrayList<>();

    // EAV for route metrics, calculated costs, etc.
}
