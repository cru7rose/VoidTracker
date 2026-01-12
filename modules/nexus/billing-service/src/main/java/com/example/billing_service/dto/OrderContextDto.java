package com.example.billing_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderContextDto {
    private UUID orderId;
    
    @JsonProperty(value = "weight")
    private BigDecimal weight; // Also accepts "totalWeight"
    
    @JsonProperty(value = "volume")
    private BigDecimal volume;
    
    @JsonProperty(value = "itemCount")
    private Integer itemCount;
    
    @JsonProperty(value = "distance")
    private BigDecimal distance; // Also accepts "distanceKm"
    
    // Custom setters to handle alternative field names
    @JsonProperty("totalWeight")
    public void setTotalWeight(BigDecimal totalWeight) {
        this.weight = totalWeight;
    }
    
    @JsonProperty("distanceKm")
    public void setDistanceKm(BigDecimal distanceKm) {
        this.distance = distanceKm;
    }
}
