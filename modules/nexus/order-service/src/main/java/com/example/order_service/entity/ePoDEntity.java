// File: order-service/src/main/java/com/example/order_service/entity/ePoDEntity.java
package com.example.order_service.entity;

import jakarta.persistence.*;

import java.time.Instant; // POPRAWKA: Dodano import
import java.util.UUID;

/**
 * ARCHITEKTURA: Encja ePoD. Typy danych dla timestamp, lat i lng zostały
 * poprawione na `Instant` i `Double` dla zachowania integralności danych.
 */
@Entity
@Table(name = "vt_epod")
public class ePoDEntity {

    public ePoDEntity() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID epodId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    private Instant timestamp; // POPRAWKA: Zmieniono typ z String na Instant
    private String userId;
    private String signature;
    private String photos;
    private Double lat; // POPRAWKA: Zmieniono typ z String na Double
    private Double lng; // POPRAWKA: Zmieniono typ z String na Double
    private String note;

    // Getters and Setters
    public UUID getEpodId() {
        return epodId;
    }

    public void setEpodId(UUID epodId) {
        this.epodId = epodId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}