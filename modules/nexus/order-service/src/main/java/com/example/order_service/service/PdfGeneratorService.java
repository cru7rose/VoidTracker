package com.example.order_service.service;

import com.example.order_service.entity.InvoiceEntity;
import com.example.order_service.entity.OrderEntity;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    public byte[] generateInvoicePdf(OrderEntity order, InvoiceEntity invoice) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            // Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            // Invoice Details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            table.addCell(getCell("Invoice Number:", PdfPCell.NO_BORDER));
            table.addCell(getCell(invoice.getInvoiceNumber(), PdfPCell.NO_BORDER));

            table.addCell(getCell("Date Issued:", PdfPCell.NO_BORDER));
            table.addCell(getCell(DateTimeFormatter.ISO_INSTANT.format(invoice.getIssuedAt()), PdfPCell.NO_BORDER));

            table.addCell(getCell("Due Date:", PdfPCell.NO_BORDER));
            table.addCell(getCell(DateTimeFormatter.ISO_INSTANT.format(invoice.getDueDate()), PdfPCell.NO_BORDER));

            table.addCell(getCell("Order ID:", PdfPCell.NO_BORDER));
            table.addCell(getCell(order.getOrderId().toString(), PdfPCell.NO_BORDER));

            document.add(table);

            document.add(Chunk.NEWLINE);

            // Customer Details
            Paragraph customerHeader = new Paragraph("Bill To:", FontFactory.getFont(FontFactory.HELVETICA_BOLD));
            document.add(customerHeader);
            document.add(new Paragraph(order.getOrderingCustomer().getCustomerName()));
            document.add(new Paragraph(order.getOrderingCustomer().getContactEmail()));

            document.add(Chunk.NEWLINE);

            // Line Items
            PdfPTable itemTable = new PdfPTable(3);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[] { 5, 2, 2 });

            itemTable.addCell(getHeaderCell("Description"));
            itemTable.addCell(getHeaderCell("Quantity"));
            itemTable.addCell(getHeaderCell("Amount"));

            itemTable.addCell("Transport Service");
            itemTable.addCell("1");
            itemTable.addCell(invoice.getAmount() + " " + invoice.getCurrency());

            document.add(itemTable);

            document.add(Chunk.NEWLINE);

            // Total
            Paragraph total = new Paragraph("Total: " + invoice.getAmount() + " " + invoice.getCurrency(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF invoice", e);
        }
    }

    private PdfPCell getCell(String text, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBorder(border);
        return cell;
    }

    private PdfPCell getHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}
