package com.example.planning_service.repository;

import com.example.planning_service.entity.BusinessRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRuleEntity, UUID> {
    Optional<BusinessRuleEntity> findByRuleCode(String ruleCode);
}
