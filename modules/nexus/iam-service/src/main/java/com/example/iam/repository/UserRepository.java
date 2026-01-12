// File: iam-app/src/main/java/com/example/iam/repository/UserRepository.java
package com.example.iam.repository;

import com.example.iam.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium rozszerzone o metodę `findAllByUserIdIn`
 * do wydajnego pobierania wielu użytkowników w jednym zapytaniu SQL.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<UserEntity> findAllByUserIdIn(List<UUID> userIds);
}