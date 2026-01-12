package com.example.planning_service.repository;

import com.example.planning_service.entity.VehicleProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleProfileRepository extends JpaRepository<VehicleProfileEntity, String> {
}
