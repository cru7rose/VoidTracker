package com.example.planning_service.repository;

import com.example.planning_service.entity.RouteAssignmentEntity;
import com.example.planning_service.entity.RouteAssignmentEntity.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for RouteAssignment persistence operations
 */
@Repository
public interface RouteAssignmentRepository extends JpaRepository<RouteAssignmentEntity, UUID> {

    /**
     * Find all route assignments for a specific optimization solution
     */
    List<RouteAssignmentEntity> findByOptimizationSolutionId(UUID solutionId);

    /**
     * Find all routes assigned to a specific driver
     */
    List<RouteAssignmentEntity> findByDriverId(UUID driverId);

    /**
     * Find all routes for a specific vehicle
     */
    List<RouteAssignmentEntity> findByVehicleId(UUID vehicleId);

    /**
     * Find all routes with a specific status
     */
    List<RouteAssignmentEntity> findByStatus(RouteStatus status);

    /**
     * Find all routes for a specific carrier
     */
    List<RouteAssignmentEntity> findByCarrierId(UUID carrierId);

    /**
     * Find routes by driver and status (e.g. all IN_PROGRESS routes for a driver)
     */
    List<RouteAssignmentEntity> findByDriverIdAndStatus(UUID driverId, RouteStatus status);
}
