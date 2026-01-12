package com.example.order_service.repository;

import com.example.order_service.entity.PricingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, UUID> {
}
