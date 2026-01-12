package com.example.order_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "vt_system_configs")
public class SystemConfigEntity {

    @Id
    @Column(name = "config_key", nullable = false)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    private Instant updatedAt;

    private String updatedBy;

    public SystemConfigEntity() {
    }

    public SystemConfigEntity(String key, String value, Instant updatedAt, String updatedBy) {
        this.key = key;
        this.value = value;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
