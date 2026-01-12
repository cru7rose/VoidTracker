package com.example.order_service.model.dto;

import lombok.Data;

/**
 * ARCHITEKTURA: DTO reprezentujący adres w odpowiedziach API.
 * Używany do zagnieżdżania informacji o adresach odbioru i dostawy w OrderResponseDto.
 */
@Data
public class AddressResponseDto {
    private String addressId;
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
}