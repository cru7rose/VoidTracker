package com.example.planning_service.repository;

import com.example.planning_service.entity.DriverTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DriverTaskRepository extends JpaRepository<DriverTaskEntity, UUID> {
    List<DriverTaskEntity> findByDriverIdAndStatus(String driverId, String status);

    List<DriverTaskEntity> findByDriverIdAndStatusNot(String driverId, String status);
}
