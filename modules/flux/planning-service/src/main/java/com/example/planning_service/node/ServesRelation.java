package com.example.planning_service.node;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@RelationshipProperties
@Data
@NoArgsConstructor
public class ServesRelation {

    @Id
    @GeneratedValue
    private Long id; // Internal Graph ID

    @TargetNode
    private LocationNode targetLocation;

    private Integer orderIndex; // Sequence in the route
    private LocalDateTime plannedEta;

    public ServesRelation(LocationNode targetLocation, Integer orderIndex) {
        this.targetLocation = targetLocation;
        this.orderIndex = orderIndex;
    }
}
