package com.example.order_service.service;

import com.example.danxils_commons.enums.OrderStatus;
import com.example.order_service.entity.InvoiceEntity;
import com.example.order_service.entity.OrderEntity;
import com.example.order_service.enums.InvoiceStatus;
import com.example.order_service.repository.InvoiceRepository;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InvoiceService.class);

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final PricingService pricingService;

    @Transactional
    public InvoiceEntity generateInvoiceForOrder(UUID orderId) {
        log.info("Generating invoice for order: {}", orderId);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (invoiceRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("Invoice already exists for order: " + orderId);
        }

        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(orderId);
        invoice.setInvoiceNumber("INV-" + System.currentTimeMillis()); // Simple generation strategy
        invoice.setAmount(pricingService.calculatePrice(order));
        invoice.setCurrency("EUR");
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedAt(Instant.now());
        invoice.setDueDate(Instant.now().plus(14, ChronoUnit.DAYS));

        // Generate PDF
        byte[] pdfBytes = pdfGeneratorService.generateInvoicePdf(order, invoice);
        // In a real scenario, upload to S3 and get URL. Here we just mock the URL.
        String pdfUrl = "https://s3.amazonaws.com/invoices/" + invoice.getInvoiceNumber() + ".pdf";
        invoice.setPdfUrl(pdfUrl);

        InvoiceEntity savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice generated successfully: {}", savedInvoice.getInvoiceNumber());
        return savedInvoice;
    }

    @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM every day
    @Transactional
    public void generateInvoicesForCompletedOrders() {
        log.info("Starting scheduled invoice generation...");
        List<OrderEntity> completedOrders = orderRepository.findByStatus(OrderStatus.POD); // Assuming POD is complete

        for (OrderEntity order : completedOrders) {
            if (!invoiceRepository.existsByOrderId(order.getOrderId())) {
                try {
                    generateInvoiceForOrder(order.getOrderId());
                } catch (Exception e) {
                    log.error("Failed to generate invoice for order {}: {}", order.getOrderId(), e.getMessage());
                }
            }
        }
        log.info("Scheduled invoice generation completed.");
    }

    public InvoiceEntity getInvoiceById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));
    }

    public byte[] getInvoicePdf(UUID invoiceId) {
        InvoiceEntity invoice = getInvoiceById(invoiceId);
        OrderEntity order = orderRepository.findById(invoice.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found for invoice: " + invoiceId));
        return pdfGeneratorService.generateInvoicePdf(order, invoice);
    }
}
