package com.example.order_service.repository;

import com.example.order_service.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {
    Optional<ClientEntity> findByName(String name);

    Optional<ClientEntity> findByNip(String nip);

    Optional<ClientEntity> findByExternalId(String externalId);
}
