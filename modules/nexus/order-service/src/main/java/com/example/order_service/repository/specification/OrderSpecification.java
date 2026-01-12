// File: order-service/src/main/java/com/example/order_service/repository/specification/OrderSpecification.java
package com.example.order_service.repository.specification;

import com.example.order_service.dto.filter.OrderFilterDto;
import com.example.order_service.entity.OrderEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * ARCHITEKTURA: Implementacja wzorca Specification dla encji OrderEntity.
 * Centralizuje i hermetyzuje logikę budowania dynamicznych zapytań do bazy danych
 * na podstawie kryteriów filtrowania. Umożliwia to tworzenie złożonych,
 * ale czytelnych i reużywalnych zapytań bez zaśmiecania interfejsu repozytorium.
 */
@Component
public class OrderSpecification {

    public Specification<OrderEntity> filterBy(OrderFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }
            if (filter.getCustomerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("orderingCustomer").get("customerId"), filter.getCustomerId()));
            }
            if (filter.getDriverId() != null && !filter.getDriverId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("assignedDriver"), filter.getDriverId()));
            }
            if (filter.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("created"), filter.getDateFrom()));
            }
            if (filter.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("created"), filter.getDateTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}