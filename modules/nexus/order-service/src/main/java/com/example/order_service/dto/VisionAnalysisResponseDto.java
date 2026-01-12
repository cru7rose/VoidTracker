package com.example.order_service.dto;

import java.util.List;

public record VisionAnalysisResponseDto(
        List<String> tags,
        String condition, // "GOOD", "DAMAGED"
        Double confidence) {
}
