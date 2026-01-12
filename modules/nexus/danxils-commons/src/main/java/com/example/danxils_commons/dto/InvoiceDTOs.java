package com.example.danxils_commons.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceDTOs {

    public record ConsolidatedInvoiceDTO(
            String tenantId,
            String invoiceNumber,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal totalAmount,
            List<DepartmentInvoiceDTO> departments) {
    }

    public record DepartmentInvoiceDTO(
            String departmentName, // Sub-profile name
            BigDecimal subTotal,
            List<LineItemDTO> lineItems) {
    }

    public record LineItemDTO(
            String description, // "Delivery to XYZ"
            String assetId,
            BigDecimal amount,
            String surchargeReason // "Overweight", "Night Delivery"
    ) {
    }
}
