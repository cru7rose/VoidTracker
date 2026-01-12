package com.example.iam.repository;

import com.example.iam.entity.RegistrationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * ARCHITEKTURA: Repozytorium JPA dla encji RegistrationTokenEntity.
 * Zapewnia standardowe operacje CRUD oraz dedykowane metody do wyszukiwania tokenu
 * na podstawie jego wartości tekstowej oraz do usuwania tokenu powiązanego z danym
 * użytkownikiem, co jest kluczowe dla procesu weryfikacji i czyszczenia danych.
 */
@Repository
public interface RegistrationTokenRepository extends JpaRepository<RegistrationTokenEntity, UUID> {
    Optional<RegistrationTokenEntity> findByToken(String token);
    void deleteByUser_UserId(UUID userId);
}