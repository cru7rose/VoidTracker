package com.example.order_service.controller;

import com.example.order_service.dto.VisionAnalysisResponseDto;
import com.example.order_service.service.VisionAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
@Tag(name = "Vision Analysis", description = "Mock Computer Vision API for damage detection")
public class VisionController {

    private final VisionAnalysisService visionAnalysisService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze an image for damage tags and optionally update order condition")
    public ResponseEntity<VisionAnalysisResponseDto> analyzeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "orderId", required = false) UUID orderId) {

        VisionAnalysisResponseDto response = visionAnalysisService.analyzeImage(file, orderId);
        return ResponseEntity.ok(response);
    }
}
