package com.example.billing_service.repository;

import com.example.billing_service.entity.RateCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RateCardRepository extends JpaRepository<RateCardEntity, UUID> {
    List<RateCardEntity> findByClientIdAndActiveTrue(String clientId);
}
