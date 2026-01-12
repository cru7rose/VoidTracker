// PLIK: address-verification-service/src/main/java/com/example/address_verification_service/service/provider/VerificationResult.java
package com.example.address_verification_service.service.provider;

import com.example.danxils_commons.dto.InternalAddressSuggestionDTO;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * ARCHITEKTURA: Wzorzec Result Object hermetyzujący wynik operacji weryfikacji adresu
 * i chroniący przed null (Przeniesione z TES.txt).
 */
@Getter
public class VerificationResult {

    public enum VerificationStatus {
        VALID,
        INVALID,
        NEEDS_REVIEW,
        SERVICE_ERROR
    }

    private final VerificationStatus status;
    private final List<InternalAddressSuggestionDTO> suggestions;
    private final String message;
    private final String rawQuery;

    public VerificationResult(VerificationStatus status, List<InternalAddressSuggestionDTO> suggestions, String message, String rawQuery) {
        this.status = status;
        this.suggestions = (suggestions != null) ? List.copyOf(suggestions) : Collections.emptyList();
        this.message = message;
        this.rawQuery = rawQuery;
    }

    public static VerificationResult error(String rawQuery, String message) {
        return new VerificationResult(VerificationStatus.SERVICE_ERROR, Collections.emptyList(), message, rawQuery);
    }

    public InternalAddressSuggestionDTO getBestMatch() {
        if (suggestions == null || suggestions.isEmpty()) {
            return null;
        }
        return suggestions.get(0);
    }
}