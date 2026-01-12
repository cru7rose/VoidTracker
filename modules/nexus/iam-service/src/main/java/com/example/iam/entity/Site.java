package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "iam_sites")
@Data
@NoArgsConstructor
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID siteId;

    @Column(nullable = false)
    private UUID orgId;

    @Enumerated(EnumType.STRING)
    private SiteType siteType;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String address;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String pickupConfig;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String omsHubConfig;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String billingConfig;

    public enum SiteType {
        WAREHOUSE, STORE, HEADQUARTERS, DROP_OFF_POINT
    }
}
