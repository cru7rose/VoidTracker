package com.example.crm_service.repository;

import com.example.crm_service.entity.ClientProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface ClientProfileRepository extends JpaRepository<ClientProfileEntity, UUID> {
    Optional<ClientProfileEntity> findByClientId(UUID clientId);
}
