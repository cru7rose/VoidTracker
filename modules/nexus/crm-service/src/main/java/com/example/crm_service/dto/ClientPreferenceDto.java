package com.example.crm_service.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ClientPreferenceDto {
    private UUID clientId;
    private String notificationEmail;
    private String preferredCarrier;
    private String defaultServiceLevel;
    private String rampHours;
}
