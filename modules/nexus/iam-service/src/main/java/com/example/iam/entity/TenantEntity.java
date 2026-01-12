package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
public class TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code; // e.g., "IKEA_GLOBAL"

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private TenantStatus status = TenantStatus.ACTIVE;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public enum TenantStatus {
        ACTIVE, SUSPENDED, ARCHIVED
    }
}
