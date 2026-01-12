package com.example.admin_panel_service.dto;

/**
 * ARCHITEKTURA: Enum reprezentujący role użytkowników. Jest to lokalna kopia
 * definicji z iam-service, używana do mapowania danych w kliencie Feign.
 * Utrzymywanie lokalnej kopii zmniejsza bezpośrednie zależności między-modułowe
 * na poziomie kodu źródłowego, choć wymaga utrzymania spójności.
 */
public enum UserRole {
    ROLE_ADMIN,
    ROLE_OPERATOR,
    ROLE_DRIVER,
    ROLE_CUSTOMER
}