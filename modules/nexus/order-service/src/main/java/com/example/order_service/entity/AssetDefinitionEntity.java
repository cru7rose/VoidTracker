package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vt_asset_definitions")
@Data
@NoArgsConstructor
public class AssetDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "asset_def_id")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    // --- New EAV Fields ---
    @Column(unique = true)
    private String code; // e.g., "IKEA_ORDER_V1"

    private Integer version;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String schema; // The JSON Schema itself

    // --- Legacy / Existing Fields ---
    private String name;
    @Column(name = "icon")
    private String iconUrl;

    @Column(columnDefinition = "text") // Using text for simplicity to match previous usage likely
    private String formSchema;

    private Double maxTemperature;
    private String workflowId; // For n8n trigger

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // Manual Shims
    public String getSchema() {
        return schema;
    }
}
