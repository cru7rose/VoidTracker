package com.example.order_service.dto.request;

import jakarta.validation.constraints.Min;

public class UpdateOrderMetricsRequestDto {
    @Min(0)
    private Integer waitingTimeMinutes;

    public UpdateOrderMetricsRequestDto() {
    }

    public UpdateOrderMetricsRequestDto(Integer waitingTimeMinutes) {
        this.waitingTimeMinutes = waitingTimeMinutes;
    }

    public Integer getWaitingTimeMinutes() {
        return waitingTimeMinutes;
    }

    public void setWaitingTimeMinutes(Integer waitingTimeMinutes) {
        this.waitingTimeMinutes = waitingTimeMinutes;
    }
}
