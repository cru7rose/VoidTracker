package com.example.planning_service.repository;

import com.example.planning_service.entity.CommunicationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunicationLogRepository extends JpaRepository<CommunicationLogEntity, UUID> {
    List<CommunicationLogEntity> findAllByOrderByTimestampDesc();
}
