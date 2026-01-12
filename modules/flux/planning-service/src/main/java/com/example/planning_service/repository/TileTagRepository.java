package com.example.planning_service.repository;

import com.example.planning_service.entity.TileTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TileTagRepository extends JpaRepository<TileTagEntity, UUID> {
    Optional<TileTagEntity> findByTagKey(String tagKey);
}
