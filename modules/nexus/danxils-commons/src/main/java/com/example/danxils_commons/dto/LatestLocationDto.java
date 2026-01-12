package com.example.danxils_commons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: DTO reprezentujące ostatnią znaną lokalizację dla danego zlecenia.
 * Przeniesione do danxils-commons jako współdzielony kontrakt danych.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestLocationDto {
    private UUID orderId;
    private Double latitude;
    private Double longitude;
    private Instant timestamp;
}