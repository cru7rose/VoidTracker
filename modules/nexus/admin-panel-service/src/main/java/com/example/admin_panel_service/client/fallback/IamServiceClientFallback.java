package com.example.admin_panel_service.client.fallback;

import com.example.admin_panel_service.client.IamServiceClient;
import com.example.admin_panel_service.dto.CreateUserRequestDto;
import com.example.admin_panel_service.dto.UpdateUserRequestDto;
import com.example.admin_panel_service.dto.UserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Implementacja logiki zastępczej (fallback) dla IamServiceClient.
 * Sygnatury metod zostały zaktualizowane, aby były zgodne z oczyszczonym
 * interfejsem klienta Feign.
 */
@Slf4j
@Component
public class IamServiceClientFallback implements IamServiceClient {

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.error("CIRCUIT BREAKER: Fallback for getAllUsers triggered. Iam-app is unavailable.");
        return Collections.emptyList();
    }

    @Override
    public UserResponseDto getUserById(UUID userId) {
        log.error("CIRCUIT BREAKER: Fallback for getUserById triggered for user {}. Iam-app is unavailable.", userId);
        return null;
    }

    @Override
    public UserResponseDto createUser(CreateUserRequestDto request) {
        log.error("CIRCUIT BREAKER: Fallback for createUser triggered. Iam-app is unavailable.");
        throw new IllegalStateException("User management service is currently unavailable. Please try again later.");
    }

    @Override
    public UserResponseDto updateUser(UUID userId, UpdateUserRequestDto request) {
        log.error("CIRCUIT BREAKER: Fallback for updateUser triggered for user {}. Iam-app is unavailable.", userId);
        throw new IllegalStateException("User management service is currently unavailable. Please try again later.");
    }

    @Override
    public void deleteUser(UUID userId) {
        log.error("CIRCUIT BREAKER: Fallback for deleteUser triggered for user {}. Iam-app is unavailable.", userId);
        throw new IllegalStateException("User management service is currently unavailable. Please try again later.");
    }
}