package com.example.danxils_commons.dto;

import com.example.danxils_commons.enums.ScanType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ScanEventDTO(
        UUID assetId,
        UUID hubId,
        ScanType scanType,
        Instant timestamp,
        Double lat,
        Double lon,
        Boolean anomaly,
        Double distance,
        Map<String, Object> metadata) {
}
