package com.example.danxils_commons.dto;

/**
 * ARCHITEKTURA: Kanoniczne DTO reprezentujące pojedynczą wymaganą usługę dodatkową.
 * Używane wewnątrz OrderResponseDto, aby poinformować klienta API (aplikację kierowcy),
 * jakie akcje są wymagane przy dostawie.
 */
public record RequiredServiceDto(
        String serviceCode,
        String name,
        String inputType
) {
}