package com.example.danxils_commons.dto;

import lombok.Data;

/**
 * ARCHITEKTURA: DTO reprezentujący klienta w odpowiedziach API.
 * Zawiera tylko niezbędne, publiczne informacje o kliencie.
 */
@Data
public class CustomerDto {
    private String customerId;
    private String customerName;
}