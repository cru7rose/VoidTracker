package com.example.planning_service.node;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Driver")
@Data
@NoArgsConstructor
public class DriverNode {

    @Id
    private String driverId; // External ID from IAM

    private String name;
    private String status; // e.g., "ACTIVE", "OFF_DUTY"
    private Double latitude;
    private Double longitude;
    private java.time.Instant lastUpdate;

    @Relationship(type = "OPERATES", direction = Relationship.Direction.OUTGOING)
    private List<RouteNode> routes = new ArrayList<>();

    public DriverNode(String driverId, String name, String status) {
        this.driverId = driverId;
        this.name = name;
        this.status = status;
    }
}
