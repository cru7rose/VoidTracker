package com.example.planning_service.repository;

import com.example.planning_service.domain.PublishedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublishedRouteRepository extends JpaRepository<PublishedRoute, String> {
    Optional<PublishedRoute> findByToken(String token);
}
