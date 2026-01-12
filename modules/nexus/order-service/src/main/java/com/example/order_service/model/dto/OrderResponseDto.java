// File: order-service/src/main/java/com/example/order_service/dto/response/OrderResponseDto.java
package com.example.order_service.model.dto;

import com.example.danxils_commons.enums.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kanoniczny, zunifikowany obiekt DTO reprezentujący pełne
 * zlecenie w odpowiedziach API. Jego struktura jest zoptymalizowana
 * dla konsumentów (frontend, mobile) i stanowi publiczny, wersjonowany
 * kontrakt,
 * oddzielony od wewnętrznej struktury encji bazodanowej.
 * Zaktualizowano pole 'packageDetails' na 'aPackage' w celu zachowania
 * spójności
 * z encją domenową i kontraktem API.
 */
public record OrderResponseDto(
                UUID orderId,
                String orderNumber, // Added
                String legacyId, // Added
                String externalReference, // Added
                String costCenter, // Added
                String department, // Added
                OrderStatus status,
                String priority,
                PickupPointDto pickup,
                DeliveryPointDto delivery,
                PackageDetailsDto aPackage,
                TimestampsDto timestamps,
                String assignedDriver,
                List<EpodDto> epods,
                Instant pickupTimeFrom,
                Instant pickupTimeTo,
                Instant deliveryTimeFrom,
                Instant deliveryTimeTo,
                String remark,
                List<String> requiredServices) {
        public record PickupPointDto(
                        String customer,
                        String alias,
                        String country,
                        Integer addressId,
                        String postalCode,
                        String city,
                        String street,
                        String streetNumber,
                        String name,
                        String attention,
                        String route,
                        String routePart,
                        String type,
                        String manifestDate,
                        Instant windowFrom,
                        Instant windowTo,
                        String mail,
                        String phone,
                        String note,
                        Double lat,
                        Double lon) {
        }

        public record DeliveryPointDto(
                        String customer,
                        String alias,
                        String country,
                        Integer addressId,
                        String postalCode,
                        String city,
                        String street,
                        String streetNumber,
                        String name,
                        String attention,
                        String route,
                        String routePart,
                        String type,
                        String manifestDate,
                        Instant sla,
                        Instant windowFrom,
                        Instant windowTo,
                        String mail,
                        String phone,
                        String note,
                        Double lat,
                        Double lon) {
        }

        public record PackageDetailsDto(
                        String barcode1,
                        String barcode2,
                        Integer colli,
                        Double weight,
                        Double volume,
                        Double routeDistance,
                        String serviceType,
                        PackageDimensionsDto packageDimensions,
                        String driverNote,
                        String invoiceNote,
                        Double price,
                        String currency,
                        Boolean adr) {
        }

        public record PackageDimensionsDto(
                        Double length,
                        Double width,
                        Double height) {
        }

        public record TimestampsDto(
                        Instant created,
                        Instant lastStatusChange) {
        }

        public record EpodDto(
                        UUID epodId,
                        Instant timestamp,
                        String userId,
                        // Pola signature i photos są celowo pominięte w DTO listy
                        // dla wydajności. Pełne dane byłyby dostępne pod dedykowanym endpointem.
                        String note) {
        }
}