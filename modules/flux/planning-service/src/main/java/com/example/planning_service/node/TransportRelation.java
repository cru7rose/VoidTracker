package com.example.planning_service.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
public class TransportRelation {
    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    private LocationNode targetLocation;

    private double distance;
    private double cost;
    private double time;
}
