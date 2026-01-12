package com.example.planning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverSessionEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(nullable = false)
    private String driverId;

    @Column(nullable = false)
    private String routeId;

    @Column(nullable = false, unique = true)
    private String token; // The magic token

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used;
}
