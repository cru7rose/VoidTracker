package com.example.planning_service.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Embeddable
@Getter
@Setter
public class LocationPoint {

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point coordinates;

    // Optional: Lat/Lon helpers if needed via transient methods
}
