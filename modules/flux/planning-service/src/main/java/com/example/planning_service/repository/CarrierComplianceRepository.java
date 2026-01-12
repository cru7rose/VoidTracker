package com.example.planning_service.repository;

import com.example.planning_service.entity.CarrierComplianceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierComplianceRepository extends JpaRepository<CarrierComplianceEntity, String> {
}
