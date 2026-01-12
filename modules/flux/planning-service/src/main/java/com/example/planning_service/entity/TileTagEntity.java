package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "tile_tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TileTagEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String tagKey; // e.g., "TAG-HIGH-PRIORITY"

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String color; // Hex code

    private String icon; // Emoji or icon code

    // JSON blob for rules to keep it flexible as per requirements
    @Column(columnDefinition = "TEXT")
    private String rulesJson;
}
