package com.example.danxils_commons.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: DTO dla Elektronicznego Potwierdzenia Dostawy (ePoD).
 * Przeniesione z order-service do danxils-commons w celu współdzielenia
 * z driver-app-service.
 */
@Data
@NoArgsConstructor
public class ePoDDto {

    private UUID epodId;

    // Pole orderId będzie pobierane z URL, nie z ciała żądania

    private Instant timestamp;

    // Pole userId będzie pobierane z kontekstu bezpieczeństwa (tokenu JWT)

    @NotBlank(message = "Signature cannot be blank")
    private String signature; // base64/png

    private List<String> photos; // Lista URLi do zdjęć lub base64

    private LocationDto location;

    private String note;

    @Data
    public static class LocationDto {
        private Double lat;
        private Double lng;
    }
}
