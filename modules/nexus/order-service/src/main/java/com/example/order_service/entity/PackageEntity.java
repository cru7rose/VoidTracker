// File: order-service/src/main/java/com/example/order_service/entity/PackageEntity.java
package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID; // POPRAWKA: Dodano import

/**
 * ARCHITEKTURA: Encja paczki. Typ klucza głównego został ustandaryzowany na
 * UUID.
 */
@Entity
@Table(name = "vt_packages")
@Data
@NoArgsConstructor
public class PackageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID packageId; // POPRAWKA: Zmieniono typ z String na UUID

    private String barcode1;
    private String barcode2;
    private Integer colli;
    private Double weight;
    private Double volume;
    private Double length;
    private Double width;
    private Double height;
    private Boolean adr;

    // --- ENHANCEMENT: New fields from Oder_Map.txt ---
    private Double routeDistance;
    private String serviceType;
    private String driverNote;
    private String invoiceNote;
    private java.math.BigDecimal price;
    private String currency;
    private String description;

    // --- Manual Getters and Setters (Lombok Fallback) ---

    public UUID getPackageId() {
        return packageId;
    }

    public void setPackageId(UUID packageId) {
        this.packageId = packageId;
    }

    public String getBarcode1() {
        return barcode1;
    }

    public void setBarcode1(String barcode1) {
        this.barcode1 = barcode1;
    }

    public String getBarcode2() {
        return barcode2;
    }

    public void setBarcode2(String barcode2) {
        this.barcode2 = barcode2;
    }

    public Integer getColli() {
        return colli;
    }

    public void setColli(Integer colli) {
        this.colli = colli;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Boolean getAdr() {
        return adr;
    }

    public void setAdr(Boolean adr) {
        this.adr = adr;
    }

    public Double getRouteDistance() {
        return routeDistance;
    }

    public void setRouteDistance(Double routeDistance) {
        this.routeDistance = routeDistance;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDriverNote() {
        return driverNote;
    }

    public void setDriverNote(String driverNote) {
        this.driverNote = driverNote;
    }

    public String getInvoiceNote() {
        return invoiceNote;
    }

    public void setInvoiceNote(String invoiceNote) {
        this.invoiceNote = invoiceNote;
    }

    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}