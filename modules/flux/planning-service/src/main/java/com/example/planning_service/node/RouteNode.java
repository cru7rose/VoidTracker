package com.example.planning_service.node;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Route")
@Data
@NoArgsConstructor
public class RouteNode {

    @Id
    private String routeId; // Unique business ID for the route

    private String name;

    @Relationship(type = "SERVES", direction = Relationship.Direction.OUTGOING)
    private List<ServesRelation> servedLocations = new ArrayList<>();

    public RouteNode(String routeId, String name) {
        this.routeId = routeId;
        this.name = name;
    }
}
