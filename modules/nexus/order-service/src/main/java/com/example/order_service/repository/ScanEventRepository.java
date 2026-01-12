package com.example.order_service.repository;

import com.example.order_service.entity.ScanEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScanEventRepository extends JpaRepository<ScanEventEntity, UUID> {
    List<ScanEventEntity> findByAssetIdOrderByTimestampDesc(UUID assetId);
}
