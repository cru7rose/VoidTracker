package com.example.danxils_commons.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BaseVoidEntity - The Foundation of VoidTracker 2026.
 * Provides standard identity, auditing, and EAV capabilities (Zero Hardcoding).
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseVoidEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * EAV Properties (Zero Hardcoding).
     * Stores flexible attributes like temperature_req, lift_req, etc.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "properties", columnDefinition = "jsonb")
    private Map<String, Object> properties = new HashMap<>();

    public void addProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }

    public <T> T getProperty(String key, Class<T> type) {
        if (this.properties == null) {
            return null;
        }
        Object value = this.properties.get(key);
        return type.isInstance(value) ? type.cast(value) : null; // Simple cast, might need ObjectMapper for complex
                                                                 // types
    }
}
