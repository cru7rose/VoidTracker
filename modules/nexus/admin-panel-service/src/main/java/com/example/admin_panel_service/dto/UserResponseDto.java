package com.example.admin_panel_service.dto;

import com.example.admin_panel_service.dto.UserRole;

import java.util.Set;
import java.util.UUID;

/**
 * ARCHITEKTURA: Dedykowany DTO dla odpowiedzi API z danymi użytkownika.
 * Stanowi kontrakt dla frontendu panelu administracyjnego i jest oddzielony od
 * wewnętrznych struktur iam-service, co pozwala na niezależną ewolucję obu
 * komponentów.
 */
public record UserResponseDto(
        UUID userId,
        String username,
        String email,
        boolean enabled,
        Set<UserRole> roles
) {
}