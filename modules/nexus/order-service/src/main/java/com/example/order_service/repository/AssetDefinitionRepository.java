package com.example.order_service.repository;

import com.example.order_service.entity.AssetDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetDefinitionRepository extends JpaRepository<AssetDefinitionEntity, UUID> {
    
    Optional<AssetDefinitionEntity> findByCode(String code);

    @Query("SELECT a FROM AssetDefinitionEntity a WHERE a.code = :code ORDER BY a.version DESC LIMIT 1")
    Optional<AssetDefinitionEntity> findLatestByCode(String code);

    Optional<AssetDefinitionEntity> findByName(String name);
}
