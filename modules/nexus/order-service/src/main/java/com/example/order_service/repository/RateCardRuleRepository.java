package com.example.order_service.repository;

import com.example.order_service.entity.RateCardRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RateCardRuleRepository extends JpaRepository<RateCardRuleEntity, UUID> {
    List<RateCardRuleEntity> findByRateCardId(UUID rateCardId);
}
