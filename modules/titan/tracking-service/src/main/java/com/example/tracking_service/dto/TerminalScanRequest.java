package com.example.tracking_service.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class TerminalScanRequest {
    private String orderId;
    private String terminalId; // e.g., "WAW-HUB-01"
    private String scanType; // "ARRIVAL", "DEPARTURE", "SORTING"
    private Instant timestamp;
    private Double latitude; // Optional, if terminal has fixed location
    private Double longitude;
}
