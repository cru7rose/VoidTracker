package com.example.danxils_commons.dto;

import com.example.danxils_commons.enums.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kanoniczny, zunifikowany obiekt DTO reprezentujący pełne
 * zlecenie w odpowiedziach API. Został rozbudowany o listę wymaganych usług
 * dodatkowych, co jest kluczowe dla logiki aplikacji kierowcy.
 * 
 * ENHANCEMENT: Dodano pola route, routePart, type do adresów zgodnie z
 * Oder_Map.txt
 */
public record OrderResponseDto(
                UUID orderId,
                OrderStatus status,
                String priority,
                AddressDto pickup,
                DeliveryAddressDto delivery,
                PackageDto aPackage,
                TimestampsDto timestamps,
                String assignedDriver,
                List<EpodDto> epods,
                List<RequiredServiceDto> requiredServices,
                String remark,

                Instant pickupTimeFrom,
                Instant pickupTimeTo,
                Instant deliveryTimeFrom,
                Instant deliveryTimeTo) {
        /**
         * ARCHITEKTURA: Zagnieżdżony, niezmienny rekord reprezentujący adres dostawy.
         * Zawiera pole `sla`, które jest kluczowe dla monitorowania terminowości.
         * ENHANCEMENT: Dodano pola route, routePart, type
         */
        public record DeliveryAddressDto(
                        String customerName,
                        String attention,
                        String street,
                        String streetNumber,
                        String postalCode,
                        String city,
                        String country,
                        String mail,
                        String phone,
                        String note,
                        Double lat,
                        Double lon,
                        Instant sla,
                        String route,
                        String routePart,
                        String type,
                        Double confidenceScore,
                        String source) {
        }

        /**
         * ARCHITEKTURA: Zagnieżdżony, niezmienny rekord przechowujący kluczowe
         * znaczniki czasu związane z cyklem życia zlecenia.
         */
        public record TimestampsDto(
                        Instant created,
                        Instant lastStatusChange) {
        }

        /**
         * ARCHITEKTURA: Zagnieżdżony, niezmienny rekord reprezentujący podsumowanie
         * Elektronicznego Potwierdzenia Dostawy (ePoD) na liście. Celowo pomija
         * ciężkie dane (podpis, zdjęcia) dla optymalizacji.
         */
        public record EpodDto(
                        UUID epodId,
                        Instant timestamp,
                        String userId,
                        String note) {
        }
}