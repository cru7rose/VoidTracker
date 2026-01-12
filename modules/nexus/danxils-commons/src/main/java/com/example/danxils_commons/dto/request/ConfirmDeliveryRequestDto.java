package com.example.danxils_commons.dto.request;

import java.util.List;

public record ConfirmDeliveryRequestDto(
        String signature,
        List<String> photos,
        Double lat,
        Double lng,
        String note,
        List<PerformedServiceDto> performedServices) {
    public record PerformedServiceDto(
            String serviceCode,
            Object result) {
    }
}
