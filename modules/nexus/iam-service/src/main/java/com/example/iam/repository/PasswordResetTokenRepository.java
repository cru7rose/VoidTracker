package com.example.iam.repository;

import com.example.iam.entity.PasswordResetTokenEntity;
import com.example.iam.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {
    Optional<PasswordResetTokenEntity> findByToken(String token);

    Optional<PasswordResetTokenEntity> findByUser(UserEntity user);

    void deleteByToken(String token);
}
