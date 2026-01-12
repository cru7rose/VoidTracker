package com.example.order_service.controller;

import com.example.order_service.dto.request.WmsOrderRequestDto;
import com.example.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/wms/orders")
@RequiredArgsConstructor
@Tag(name = "WMS Integration", description = "Endpoints for WMS integration (e.g., BMW Wheels)")
public class WmsOrderController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WmsOrderController.class);

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order from WMS notification")
    public ResponseEntity<UUID> createOrderFromWms(@Valid @RequestBody WmsOrderRequestDto wmsOrderRequest) {
        log.info("Received WMS order request: {}", wmsOrderRequest);
        UUID orderId = orderService.createOrderFromWms(wmsOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }
}
