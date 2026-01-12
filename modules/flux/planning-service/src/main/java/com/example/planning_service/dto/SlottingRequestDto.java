package com.example.planning_service.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class SlottingRequestDto {
    private UUID orderId;
    private double weight;
    private double volume;
    private double lat;
    private double lon;
}
