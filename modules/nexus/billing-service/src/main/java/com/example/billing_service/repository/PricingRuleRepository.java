package com.example.billing_service.repository;

import com.example.billing_service.entity.PricingRuleEntity;
import com.example.billing_service.entity.RateCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, UUID> {
    List<PricingRuleEntity> findByRateCard(RateCardEntity rateCard);
}
