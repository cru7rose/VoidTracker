// PLIK: danxils-commons/src/main/java/com/example/danxils_commons/dto/ProviderVerificationResultDTO.java
package com.example.danxils_commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * ARCHITEKTURA: Kanoniczne DTO dla odpowiedzi z serwisu weryfikacji adresów.
 * Jako część modułu commons, stanowi współdzielony kontrakt danych dla całego ekosystemu.
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