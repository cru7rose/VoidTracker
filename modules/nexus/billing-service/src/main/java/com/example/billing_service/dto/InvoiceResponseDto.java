package com.example.billing_service.dto;

import com.example.billing_service.entity.InvoiceEntity;
import com.example.billing_service.entity.InvoiceLineItemEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: DTO for invoice response with line items.
 */
@Data
@Builder
public class InvoiceResponseDto {
    private UUID id;
    private String invoiceNumber;
    private String clientId;
    private UUID orderId;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDate issuedDate;
    private LocalDate dueDate;
    private Instant paidDate;
    private String paymentReference;
    private List<LineItemDto> lineItems;
    private String notes;
    private Instant createdAt;

    @Data
    @Builder
    public static class LineItemDto {
        private Integer lineNumber;
        private String description;
        private String itemType;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    /**
     * Convert Invoice entity to DTO.
     */
    public static InvoiceResponseDto fromEntity(InvoiceEntity invoice) {
        return InvoiceResponseDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .clientId(invoice.getClientId())
                .orderId(invoice.getOrderId())
                .status(invoice.getStatus().name())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .currency(invoice.getCurrency())
                .issuedDate(invoice.getIssuedDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())
                .paymentReference(invoice.getPaymentReference())
                .lineItems(invoice.getLineItems().stream()
                        .map(InvoiceResponseDto::lineItemFromEntity)
                        .collect(Collectors.toList()))
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    private static LineItemDto lineItemFromEntity(InvoiceLineItemEntity item) {
        return LineItemDto.builder()
                .lineNumber(item.getLineNumber())
                .description(item.getDescription())
                .itemType(item.getItemType())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
