package com.example.order_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ARCHITEKTURA: Standardowy obiekt transferu danych (DTO) dla odpowiedzi błędów API.
 * Zapewnia spójny i przewidywalny format komunikacji o błędach, zawierający kod błędu,
 * ogólną wiadomość oraz opcjonalne szczegóły techniczne. Jest to kluczowy element
 * dobrze zaprojektowanego, deweloperskiego kontraktu API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorDto {
    private String errorCode;
    private String message;
    private String details;
}