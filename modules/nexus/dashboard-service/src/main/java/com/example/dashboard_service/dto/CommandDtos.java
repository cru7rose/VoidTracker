package com.example.dashboard_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CommandDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommandRequestDto {
        private String query;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommandResponseDto {
        private String action; // e.g., 'NAVIGATE', 'ALERT'
        private String target; // e.g., '/lenses', 'Filtered High Risk'
        private String message;
    }
}
