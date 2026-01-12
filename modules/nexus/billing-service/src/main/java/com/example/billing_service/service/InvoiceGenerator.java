package com.example.billing_service.service;

import com.example.billing_service.dto.OrderContextDto;
import com.example.billing_service.entity.*;
import com.example.billing_service.exception.InvalidInvoiceStateException;
import com.example.billing_service.exception.RateCardNotFoundException;
import com.example.billing_service.repository.InvoiceRepository;
import com.example.billing_service.repository.PricingRuleRepository;
import com.example.billing_service.repository.RateCardRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Service for generating invoices with itemized breakdown.
 * Creates invoice entities with line items based on pricing rule application.
 */
@Service
@Slf4j
public class InvoiceGenerator {

    private final PricingEngine pricingEngine;
    private final InvoiceRepository invoiceRepository;
    private final RateCardRepository rateCardRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final Timer invoiceGenerationTimer;
    private final Counter invoiceCounter;

    public InvoiceGenerator(PricingEngine pricingEngine,
                           InvoiceRepository invoiceRepository,
                           RateCardRepository rateCardRepository,
                           PricingRuleRepository pricingRuleRepository,
                           MeterRegistry meterRegistry) {
        this.pricingEngine = pricingEngine;
        this.invoiceRepository = invoiceRepository;
        this.rateCardRepository = rateCardRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.invoiceGenerationTimer = meterRegistry.timer("billing.invoice.generation.time");
        this.invoiceCounter = meterRegistry.counter("billing.invoice.generated");
    }

    /**
     * Generate invoice for a single order.
     * 
     * @param clientId Client identifier
     * @param order Order context with weight, distance, etc.
     * @return Generated invoice entity
     */
    @Transactional
    public InvoiceEntity generateInvoice(String clientId, OrderContextDto order) {
        log.info("Generating invoice for client: {}, order: {}", clientId, order.getOrderId());

        // 1. Find active rate card
        List<RateCardEntity> rateCards = rateCardRepository.findByClientIdAndActiveTrue(clientId);
        if (rateCards.isEmpty()) {
            throw new RateCardNotFoundException(clientId);
        }
        RateCardEntity rateCard = rateCards.get(0);

        // 2. Create invoice header
        InvoiceEntity invoice = InvoiceEntity.builder()
                .invoiceNumber(generateInvoiceNumber(clientId))
                .clientId(clientId)
                .orderId(order.getOrderId())
                .status(InvoiceEntity.InvoiceStatus.DRAFT)
                .currency(rateCard.getCurrency())
                .issuedDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30)) // Default: 30 days payment terms
                .createdAt(Instant.now())
                .build();

        // 3. Fetch pricing rules and generate line items
        List<PricingRuleEntity> rules = pricingRuleRepository.findByRateCard(rateCard);
        int lineNumber = 1;

        for (PricingRuleEntity rule : rules) {
            BigDecimal metricValue = getMetricValue(order, rule.getMetric());
            
            // Check if rule applies
            boolean inRange = metricValue.compareTo(rule.getRangeStart()) >= 0
                    && (rule.getRangeEnd() == null || metricValue.compareTo(rule.getRangeEnd()) < 0);

            if (inRange && metricValue.compareTo(BigDecimal.ZERO) > 0) {
                InvoiceLineItemEntity lineItem = createLineItem(rule, metricValue, lineNumber++, order.getOrderId());
                invoice.addLineItem(lineItem);
            }
        }

        // 4. Calculate totals
        invoice.calculateTotals();

        // 5. Save invoice
        InvoiceEntity savedInvoice = invoiceRepository.save(invoice);
        invoiceCounter.increment();
        log.info("Invoice {} created successfully. Total: {} {}", 
                savedInvoice.getInvoiceNumber(), 
                savedInvoice.getTotalAmount(), 
                savedInvoice.getCurrency());

        return savedInvoice;
    }

    /**
     * Issue invoice (change status from DRAFT to ISSUED).
     */
    @Transactional
    public InvoiceEntity issueInvoice(UUID invoiceId) {
        InvoiceEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        
        if (invoice.getStatus() != InvoiceEntity.InvoiceStatus.DRAFT) {
            throw new InvalidInvoiceStateException(
                String.format("Only DRAFT invoices can be issued. Current status: %s", invoice.getStatus()));
        }

        invoice.setStatus(InvoiceEntity.InvoiceStatus.ISSUED);
        invoice.setUpdatedAt(Instant.now());
        
        InvoiceEntity updated = invoiceRepository.save(invoice);
        log.info("Invoice {} issued successfully", updated.getInvoiceNumber());
        
        return updated;
    }

    /**
     * Mark invoice as paid.
     */
    @Transactional
    public InvoiceEntity markAsPaid(UUID invoiceId, String paymentReference) {
        InvoiceEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        
        invoice.setStatus(InvoiceEntity.InvoiceStatus.PAID);
        invoice.setPaidDate(Instant.now());
        invoice.setPaymentReference(paymentReference);
        invoice.setUpdatedAt(Instant.now());
        
        InvoiceEntity updated = invoiceRepository.save(invoice);
        log.info("Invoice {} marked as paid. Payment ref: {}", updated.getInvoiceNumber(), paymentReference);
        
        return updated;
    }

    /**
     * Generate unique invoice number in format: INV-YYYY-MM-######
     * Example: INV-2025-12-000001
     */
    private String generateInvoiceNumber(String clientId) {
        LocalDate today = LocalDate.now();
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        // Count invoices issued today for this client to generate sequential number
        long countToday = invoiceRepository.countByIssuedDateAndClientId(today, clientId);
        long nextNumber = countToday + 1;
        
        return String.format("INV-%s-%06d", yearMonth, nextNumber);
    }

    /**
     * Create line item from pricing rule application.
     */
    private InvoiceLineItemEntity createLineItem(PricingRuleEntity rule, BigDecimal metricValue, int lineNumber, UUID orderId) {
        String metricName = rule.getMetric().toString();
        String unit = getUnitForMetric(rule.getMetric());
        
        BigDecimal unitPrice;
        BigDecimal totalPrice;
        String description;

        if (rule.getUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
            // Variable pricing: base + (value × unit price)
            unitPrice = rule.getUnitPrice();
            totalPrice = rule.getBasePrice().add(metricValue.multiply(unitPrice));
            description = String.format("%s Charge (%.2f %s @ €%.2f/%s + €%.2f base)", 
                    metricName, 
                    metricValue, 
                    unit, 
                    unitPrice, 
                    unit,
                    rule.getBasePrice());
        } else {
            // Flat pricing
            unitPrice = rule.getBasePrice();
            totalPrice = rule.getBasePrice();
            description = String.format("%s Charge (flat rate)", metricName);
        }

        return InvoiceLineItemEntity.builder()
                .lineNumber(lineNumber)
                .description(description)
                .itemType(metricName)
                .quantity(metricValue)
                .unit(unit)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .orderId(orderId)
                .build();
    }

    /**
     * Extract metric value from order context.
     */
    private BigDecimal getMetricValue(OrderContextDto order, PricingRuleEntity.MetricType metric) {
        switch (metric) {
            case WEIGHT: return order.getWeight() != null ? order.getWeight() : BigDecimal.ZERO;
            case VOLUME: return order.getVolume() != null ? order.getVolume() : BigDecimal.ZERO;
            case COUNT: return order.getItemCount() != null ? BigDecimal.valueOf(order.getItemCount()) : BigDecimal.ZERO;
            case DISTANCE: return order.getDistance() != null ? order.getDistance() : BigDecimal.ZERO;
            case ITEM: return BigDecimal.ONE;
            default: return BigDecimal.ZERO;
        }
    }

    /**
     * Get display unit for metric type.
     */
    private String getUnitForMetric(PricingRuleEntity.MetricType metric) {
        switch (metric) {
            case WEIGHT: return "kg";
            case VOLUME: return "m³";
            case COUNT: return "items";
            case DISTANCE: return "km";
            case ITEM: return "order";
            default: return "";
        }
    }
}
