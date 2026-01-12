package com.example.dashboard_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandResponse {
    private String action; // NAVIGATE, MESSAGE
    private String target; // URL or Route
    private String message;
}
