package com.example.order_service.entity;

import com.example.danxils_commons.entity.BaseVoidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Client Entity (Void-Core).
 * Represents a simplified, EAV-enhanced client profile.
 */
@Entity
@Table(name = "vt_client")
@Getter
@Setter
public class ClientEntity extends BaseVoidEntity {

    @Column(nullable = false)
    private String name;

    private String nip;

    @Column(name = "trust_score")
    private Double trustScore;

    @Column(name = "external_id", unique = true)
    private String externalId;

    // Shims
    public String getName() {
        return name;
    } // Manual Getter

    public void setName(String name) {
        this.name = name;
    } // Manual Setter

    public String getCustomerName() {
        return getName();
    }

    public String getContactEmail() {
        return getProperty("email", String.class);
    }

    // --- Soft Fields (EAV wrappers) ---

    public String getRampHours() {
        return getProperty("ramp_hours", String.class);
    }

    public void setRampHours(String hours) {
        addProperty("ramp_hours", hours);
    }

    // Example of managing a list via JSONB (requires complex logic or simplified
    // getter)
    // For now, simpler accessors
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
