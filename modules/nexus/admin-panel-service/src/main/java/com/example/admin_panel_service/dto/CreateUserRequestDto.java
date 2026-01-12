package com.example.admin_panel_service.dto;

import java.util.Set;

/**
 * ARCHITEKTURA: Dedykowany DTO dla żądania utworzenia nowego użytkownika
 * z poziomu panelu administracyjnego. Definiuje kontrakt wejściowy dla API,
 * zawierając minimalny zestaw danych wymaganych do zainicjowania procesu
 * rejestracji w iam-service.
 */
public record CreateUserRequestDto(
        String email,
        Set<UserRole> roles
) {
}