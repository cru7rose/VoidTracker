package com.example.billing_service.controller;

import com.example.billing_service.dto.CalculationBreakdownDto;
import com.example.billing_service.dto.InvoiceResponseDto;
import com.example.billing_service.dto.OrderContextDto;
import com.example.billing_service.entity.BillingProfileEntity;
import com.example.billing_service.entity.InvoiceEntity;
import com.example.billing_service.repository.BillingProfileRepository;
import com.example.billing_service.repository.InvoiceRepository;
import com.example.billing_service.service.InvoiceGenerator;
import com.example.billing_service.service.PricingEngine;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final PricingEngine pricingEngine;
    private final InvoiceGenerator invoiceGenerator;
    private final BillingProfileRepository profileRepository;
    private final InvoiceRepository invoiceRepository;

    @PostMapping("/calculate-preview")
    public ResponseEntity<BigDecimal> calculatePreview(
            @RequestParam String clientId,
            @RequestBody OrderContextDto order) {
        return ResponseEntity.ok(pricingEngine.calculatePrice(clientId, order));
    }

    @PostMapping("/calculate-breakdown")
    public ResponseEntity<CalculationBreakdownDto> calculateBreakdown(
            @RequestParam String clientId,
            @RequestBody OrderContextDto order) {
        return ResponseEntity.ok(pricingEngine.calculatePriceWithBreakdown(clientId, order));
    }

    @PostMapping("/invoices/generate")
    public ResponseEntity<InvoiceResponseDto> generateInvoice(
            @RequestParam String clientId,
            @RequestBody OrderContextDto order) {
        InvoiceEntity invoice = invoiceGenerator.generateInvoice(clientId, order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InvoiceResponseDto.fromEntity(invoice));
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<InvoiceResponseDto> getInvoice(@PathVariable UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(InvoiceResponseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invoices/order/{orderId}")
    public ResponseEntity<InvoiceResponseDto> getInvoiceByOrder(@PathVariable UUID orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .map(InvoiceResponseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invoices/client/{clientId}")
    public ResponseEntity<List<InvoiceResponseDto>> getClientInvoices(@PathVariable String clientId) {
        List<InvoiceResponseDto> invoices = invoiceRepository.findByClientId(clientId).stream()
                .map(InvoiceResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoices);
    }

    @PostMapping("/invoices/{invoiceId}/issue")
    public ResponseEntity<InvoiceResponseDto> issueInvoice(@PathVariable UUID invoiceId) {
        InvoiceEntity invoice = invoiceGenerator.issueInvoice(invoiceId);
        return ResponseEntity.ok(InvoiceResponseDto.fromEntity(invoice));
    }

    @PostMapping("/invoices/{invoiceId}/mark-paid")
    public ResponseEntity<InvoiceResponseDto> markAsPaid(
            @PathVariable UUID invoiceId,
            @RequestParam String paymentReference) {
        InvoiceEntity invoice = invoiceGenerator.markAsPaid(invoiceId, paymentReference);
        return ResponseEntity.ok(InvoiceResponseDto.fromEntity(invoice));
    }
}
