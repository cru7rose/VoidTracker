package com.example.danxils_commons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * ARCHITEKTURA: DTO reprezentujący pojedynczy punkt na historycznej trasie
 * zlecenia. Przeniesione do danxils-commons jako współdzielony kontrakt danych.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationHistoryDto {
    private Double latitude;
    private Double longitude;
    private Instant timestamp;
}