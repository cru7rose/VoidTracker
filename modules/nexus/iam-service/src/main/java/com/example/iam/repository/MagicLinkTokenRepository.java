package com.example.iam.repository;

import com.example.iam.entity.MagicLinkTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkTokenEntity, UUID> {
    Optional<MagicLinkTokenEntity> findByToken(String token);
}
