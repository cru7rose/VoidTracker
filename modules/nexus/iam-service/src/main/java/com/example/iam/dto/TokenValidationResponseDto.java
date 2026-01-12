package com.example.iam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * ARCHITEKTURA: Obiekt Transferu Danych (DTO) używany jako odpowiedź z
 * wewnętrznego
 * endpointu walidacji tokenu. Dostarcza wywołującemu serwisowi niezbędnych,
 * bezpiecznych informacji o tożsamości użytkownika, na podstawie których
 * może on zbudować własny kontekst bezpieczeństwa.
 */
@Data
@Builder
public class TokenValidationResponseDto {
    private boolean valid;
    private String userId;
    private String username;
    private Set<String> roles;
}