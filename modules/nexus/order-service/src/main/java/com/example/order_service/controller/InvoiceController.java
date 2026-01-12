package com.example.order_service.controller;

import com.example.order_service.entity.InvoiceEntity;
import com.example.order_service.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/generate/{orderId}")
    public ResponseEntity<InvoiceEntity> generateInvoice(@PathVariable UUID orderId) {
        return ResponseEntity.ok(invoiceService.generateInvoiceForOrder(orderId));
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable UUID invoiceId) {
        byte[] pdfBytes = invoiceService.getInvoicePdf(invoiceId);
        InvoiceEntity invoice = invoiceService.getInvoiceById(invoiceId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", invoice.getInvoiceNumber() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
