package com.example.planning_service.repository;

import com.example.planning_service.entity.RouteStopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStopEntity, UUID> {
}
