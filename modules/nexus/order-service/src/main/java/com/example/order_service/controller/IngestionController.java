package com.example.order_service.controller;

import com.example.order_service.dto.IngestionRequestDto;
import com.example.order_service.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping("/orders")
    public ResponseEntity<String> ingestOrder(@RequestBody IngestionRequestDto request) {
        String orderId = ingestionService.ingestOrder(request);
        return ResponseEntity.ok(orderId);
    }
}
