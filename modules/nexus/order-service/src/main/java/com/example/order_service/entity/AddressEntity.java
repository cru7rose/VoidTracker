package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Address Entity.
 * Now optionally linked to ClientEntity instead of CustomerEntity.
 */
@Entity
@Table(name = "vt_addresses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "address_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_customer_id", nullable = false)
    private ClientEntity ownerClient;

    private String customerName; // Name of the person at address
    private String attention;
    private String street;
    private String streetNumber;
    private String apartment;
    private String city;
    private String postalCode;
    private String country;

    private Double lat;
    private Double lon;

    private String mail;
    private String phone;
    private String note;

    // SLA for delivery addresses
    private java.time.Instant sla;

    // Validation / Healing
    private Double confidenceScore;
    private String source;

    public Integer getAddressId() {
        return id != null ? id.hashCode() : 0;
    } // Shim

    // Legacy / DTO fields
    private String route;
    private String routePart;
    private String type;

    // Helper for easy DTO mapping
    public void setOwner(ClientEntity client) {
        this.ownerClient = client;
    }

    // --- Manual Getters and Setters (Lombok Fallback) ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ClientEntity getOwnerClient() {
        return ownerClient;
    }

    public void setOwnerClient(ClientEntity ownerClient) {
        this.ownerClient = ownerClient;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAttention() {
        return attention;
    }

    public void setAttention(String attention) {
        this.attention = attention;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public java.time.Instant getSla() {
        return sla;
    }

    public void setSla(java.time.Instant sla) {
        this.sla = sla;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getRoutePart() {
        return routePart;
    }

    public void setRoutePart(String routePart) {
        this.routePart = routePart;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}