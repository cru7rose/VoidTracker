package com.example.billing_service.repository;

import com.example.billing_service.entity.BillingProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BillingProfileRepository extends JpaRepository<BillingProfileEntity, UUID> {
    List<BillingProfileEntity> findByNextRunDateBeforeAndFrequencyNot(LocalDate date, BillingProfileEntity.CycleFrequency frequency);
}
