package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "iam_organizations")
@Data
@NoArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orgId;

    @Column(nullable = false)
    private String legalName;

    private String vatId;

    @Enumerated(EnumType.STRING)
    private OrganizationStatus status;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String configuration;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String billingConfig;

    public enum OrganizationStatus {
        ACTIVE, SUSPENDED, ONBOARDING
    }
}
