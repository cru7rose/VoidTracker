package com.example.order_service.repository;

import com.example.order_service.entity.CustomerHarmonogramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerHarmonogramRepository extends JpaRepository<CustomerHarmonogramEntity, UUID> {
    Optional<CustomerHarmonogramEntity> findByClient_Id(UUID clientId);

    Optional<CustomerHarmonogramEntity> findByAlias(String alias);
}
