package com.example.order_service.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ARCHITEKTURA: Instancja zasobu (Asset Instance).
 * Reprezentuje konkretny przedmiot w zleceniu.
 * Przechowuje dynamiczne atrybuty w formacie JSONB.
 */
@Entity
@Table(name = "vt_assets")
@NoArgsConstructor
public class AssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "asset_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_def_id", nullable = true)
    private AssetDefinitionEntity assetDefinition;

    /**
     * Wartości atrybutów dla danego zasobu.
     * Np.: {"weight_kg": 45, "is_wet": true}
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AssetEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<AssetEntity> children = new ArrayList<>();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AssetDefinitionEntity getAssetDefinition() {
        return assetDefinition;
    }

    public void setAssetDefinition(AssetDefinitionEntity assetDefinition) {
        this.assetDefinition = assetDefinition;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public AssetEntity getParent() {
        return parent;
    }

    public void setParent(AssetEntity parent) {
        this.parent = parent;
    }

    public List<AssetEntity> getChildren() {
        return children;
    }

    public void setChildren(List<AssetEntity> children) {
        this.children = children;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }
}
