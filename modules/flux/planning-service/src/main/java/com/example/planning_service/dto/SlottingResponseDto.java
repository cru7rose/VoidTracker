package com.example.planning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SlottingResponseDto {
    private boolean success;
    private UUID routeId;
    private String message;
}
