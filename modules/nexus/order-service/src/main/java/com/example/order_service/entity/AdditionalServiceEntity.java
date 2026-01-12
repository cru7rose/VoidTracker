package com.example.order_service.entity;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * ARCHITEKTURA: Encja JPA dla słownika usług dodatkowych.
 * Definiuje podstawowe atrybuty każdej możliwej do wykonania usługi.
 */
@Entity
@Table(name = "additional_services", schema = "public")
public class AdditionalServiceEntity {
    // Recompile trigger

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String serviceCode;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceInputType inputType;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ServiceInputType getInputType() {
        return inputType;
    }

    public void setInputType(ServiceInputType inputType) {
        this.inputType = inputType;
    }
}