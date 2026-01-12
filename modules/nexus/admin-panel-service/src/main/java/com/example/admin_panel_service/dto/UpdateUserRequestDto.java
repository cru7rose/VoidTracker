package com.example.admin_panel_service.dto;

import java.util.Set;

/**
 * ARCHITEKTURA: Dedykowany DTO dla żądania aktualizacji danych istniejącego
 * użytkownika. Definiuje kontrakt wejściowy dla API, określając, które pola
 * mogą być modyfikowane przez administratora.
 */
public record UpdateUserRequestDto(
        String username,
        String email,
        Set<UserRole> roles,
        boolean enabled
) {
}