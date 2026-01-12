package com.example.order_service.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a Hub (Warehouse/Terminal) in the network.
 * Holds the sorting logic for the WMS.
 */
@Entity
@Table(name = "vt_hubs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "location_code", nullable = false, unique = true)
    private String locationCode; // e.g. "HUB-BER-01"

    /**
     * Stored as JSON to avoid complex Address entity coupling if not needed.
     * Can be mapped to AddressDto.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> address;

    /**
     * The "Brain" of the WMS.
     * Maps Destination Logic to Physical Zones.
     * Example:
     * {
     * "rules": [
     * {"zip_prefix": "10", "zone": "LANE-1-BERLIN"},
     * {"zip_prefix": "20", "zone": "LANE-2-HAMBURG"}
     * ]
     * }
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", name = "sorting_rules")
    private Map<String, Object> sortingRules;

}
