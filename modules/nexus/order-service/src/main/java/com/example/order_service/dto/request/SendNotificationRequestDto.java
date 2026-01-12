package com.example.order_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequestDto {
    @NotBlank
    private String userId;

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    private Map<String, String> data;
}
