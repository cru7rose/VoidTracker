package com.example.planning_service.repository.graph;

import com.example.planning_service.node.RouteNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GraphRouteRepository extends Neo4jRepository<RouteNode, String> {
}
