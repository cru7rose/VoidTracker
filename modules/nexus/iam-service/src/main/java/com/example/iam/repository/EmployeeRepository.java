package com.example.iam.repository;

import com.example.iam.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {
    Optional<EmployeeEntity> findByUserUserId(UUID userId);

    Optional<EmployeeEntity> findByLegacyId(String legacyId);
}
