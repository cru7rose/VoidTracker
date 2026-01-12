package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "view_configurations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewConfigurationEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String viewId; // e.g., "DISPATCH_BOARD_V1"

    @Column(columnDefinition = "TEXT")
    private String configJson; // JSON blob for columns, fields, refresh rates
}
