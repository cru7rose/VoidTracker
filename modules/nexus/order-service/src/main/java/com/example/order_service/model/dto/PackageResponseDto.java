package com.example.order_service.model.dto;

import lombok.Data;

/**
 * ARCHITEKTURA: DTO reprezentujący paczkę w odpowiedziach API.
 * Zawiera szczegółowe informacje o przesyłce.
 */
@Data
public class PackageResponseDto {
    private String packageId;
    private String barcode1;
    private String barcode2;
    private Integer colli;
    private Double weight;
    private Double volume;
    private Double length;
    private Double width;
    private Double height;
    private Boolean adr;
}