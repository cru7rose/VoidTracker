package com.example.planning_service.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Location")
@Data
public class LocationNode {
    @Id
    private String id; // External ID or Name
    private String name;
    private String type; // HUB, SPOKE

    @Relationship(type = "CONNECTED_TO", direction = Relationship.Direction.OUTGOING)
    private List<TransportRelation> connections = new ArrayList<>();

    public LocationNode(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}
