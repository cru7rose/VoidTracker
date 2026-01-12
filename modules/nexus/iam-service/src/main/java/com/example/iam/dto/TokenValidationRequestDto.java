package com.example.iam.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ARCHITEKTURA: Obiekt Transferu Danych (DTO) dla żądań przychodzących do wewnętrznego
 * endpointu walidacji tokenu. Hermetyzuje token JWT w ciele żądania.
 */
@Data
@NoArgsConstructor
public class TokenValidationRequestDto {
    private String token;
}