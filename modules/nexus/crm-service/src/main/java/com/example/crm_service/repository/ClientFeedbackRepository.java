package com.example.crm_service.repository;

import com.example.crm_service.entity.ClientFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ClientFeedbackRepository extends JpaRepository<ClientFeedbackEntity, UUID> {
    List<ClientFeedbackEntity> findByClientId(UUID clientId);
}
