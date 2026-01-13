package com.example.planning_service.repository.specification;

import com.example.planning_service.dto.RouteAssignmentFilterDto;
import com.example.planning_service.entity.RouteAssignmentEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification pattern for RouteAssignment filtering.
 * Designed for high-volume queries (thousands of orders per day, multiple clients).
 * Supports complex filtering with multiple criteria for efficient database queries.
 */
@Component
public class RouteAssignmentSpecification {

    /**
     * Builds a JPA Specification from filter DTO.
     * All filters are combined with AND logic.
     */
    public Specification<RouteAssignmentEntity> filterBy(RouteAssignmentFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by status(es)
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            // Filter by driver ID
            if (filter.getDriverId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("driverId"), filter.getDriverId()));
            }

            // Filter by multiple driver IDs (for bulk queries)
            if (filter.getDriverIds() != null && !filter.getDriverIds().isEmpty()) {
                predicates.add(root.get("driverId").in(filter.getDriverIds()));
            }

            // Filter by vehicle ID
            if (filter.getVehicleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicleId"), filter.getVehicleId()));
            }

            // Filter by carrier ID
            if (filter.getCarrierId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("carrierId"), filter.getCarrierId()));
            }

            // Filter by multiple carrier IDs (multi-tenant scenarios)
            if (filter.getCarrierIds() != null && !filter.getCarrierIds().isEmpty()) {
                predicates.add(root.get("carrierId").in(filter.getCarrierIds()));
            }

            // Filter by optimization solution ID
            if (filter.getSolutionId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("optimizationSolutionId"), filter.getSolutionId()));
            }

            // Date range filters for created_at
            if (filter.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAfter()));
            }
            if (filter.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedBefore()));
            }

            // Date range filters for updated_at
            if (filter.getUpdatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedAfter()));
            }
            if (filter.getUpdatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("updatedAt"), filter.getUpdatedBefore()));
            }

            // Partial match on route name (case-insensitive)
            if (filter.getRouteNameContains() != null && !filter.getRouteNameContains().isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("routeName")),
                    "%" + filter.getRouteNameContains().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
