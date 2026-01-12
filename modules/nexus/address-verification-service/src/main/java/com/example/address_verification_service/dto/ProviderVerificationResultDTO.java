// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/dto/ProviderVerificationResultDTO.java
package com.example.address_verification_service.dto;

import com.example.address_verification_service.dto.provider.InternalAddressSuggestionDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ARCHITEKTURA: DTO API dla klienta zewnętrznego (BFF), oparte na wewnętrznym kontrakcie.
 * [cite_start]Zapewnia spójną strukturę odpowiedzi (Przeniesione z TES.txt [cite: 1698]).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderVerificationResultDTO {
    public enum VerificationStatus {
        VALID,
        INVALID,
        NEEDS_REVIEW,
        SERVICE_ERROR
    }

    private VerificationStatus status;
    private List<InternalAddressSuggestionDTO> suggestions;
    private String message;
    private String rawQuery;

    public static ProviderVerificationResultDTO error(String rawQuery, String message) {
        return ProviderVerificationResultDTO.builder()
                .status(VerificationStatus.SERVICE_ERROR)
                .suggestions(List.of())
                .message(message)
                .rawQuery(rawQuery)
                .build();
    }
}