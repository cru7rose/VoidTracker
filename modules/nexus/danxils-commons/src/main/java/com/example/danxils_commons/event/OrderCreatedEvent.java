// File: danxils-commons/src/main/java/com/example/danxils_commons/event/OrderCreatedEvent.java
package com.example.danxils_commons.event;

import com.example.danxils_commons.dto.AddressDto;
import com.example.danxils_commons.dto.PackageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * ARCHITEKTURA: Scentralizowana, wersjonowana definicja zdarzenia o utworzeniu zlecenia.
 * Stanowi część publicznego kontraktu współdzielonego między mikroserwisami.
 * Została zaktualizowana, aby zawierać pole `sla` w adresie dostawy,
 * co jest kluczowe dla modułów analitycznych i monitorujących.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private String orderId;
    private String customerId;
    private String priority;
    private AddressDto pickupAddress;
    private DeliveryAddressDto deliveryAddress;
    private PackageDto packageDetails;

    /**
     * ARCHITEKTURA: Zagnieżdżony DTO wewnątrz zdarzenia, aby jednoznacznie
     * przenieść informację o wymaganym czasie dostawy (SLA).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAddressDto {
        private String customerName;
        private String attention;
        private String street;
        private String streetNumber;
        private String postalCode;
        private String city;
        private String country;
        private String mail;
        private String phone;
        private String note;
        private Double lat;
        private Double lon;
        private Instant sla; // NOWE POLE
    }
}