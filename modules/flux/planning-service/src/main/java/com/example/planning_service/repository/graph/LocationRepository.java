package com.example.planning_service.repository.graph;

import com.example.planning_service.node.LocationNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends Neo4jRepository<LocationNode, String> {

    @Query("MATCH p=shortestPath((a:Location {id: $startId})-[*]-(b:Location {id: $endId})) RETURN nodes(p)")
    List<LocationNode> findShortestPath(String startId, String endId);
}
