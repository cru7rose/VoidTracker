package com.example.planning_service.repository.graph;

import com.example.planning_service.node.DriverNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GraphDriverRepository extends Neo4jRepository<DriverNode, String> {
}
