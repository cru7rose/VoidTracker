package com.example.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ARCHITEKTURA: DTO dla odpowiedzi z API analitycznego.
 * Reprezentuje pojedynczy wskaźnik KPI.
 */
@Data
@AllArgsConstructor
public class KpiDto {
    private String name;
    private Object value;
    private String description;
}