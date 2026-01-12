package com.example.order_service.dto;

import lombok.Data;
import java.util.Map;

@Data
public class IngestionRequestDto {
    private String externalId; // Order ID in external system (e.g., ERP-1001)
    private String clientExternalId; // Link to Client via external ID
    private Map<String, Object> properties; // Dynamic fields (weight, volume, etc.)

    // Manual Shims
    public String getExternalId() {
        return externalId;
    }

    public String getClientExternalId() {
        return clientExternalId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
