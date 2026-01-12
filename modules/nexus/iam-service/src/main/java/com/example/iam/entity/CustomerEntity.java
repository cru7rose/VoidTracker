package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "iam_customers")
@Data
@NoArgsConstructor
public class CustomerEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_customer_id")
    private CustomerEntity parentCustomer;

    @Column(name = "is_address_book_owner")
    private Boolean isAddressBookOwner = false;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "legacy_id")
    private String legacyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_info", columnDefinition = "TEXT")
    private String contactInfo; // JSON string

    @Column(columnDefinition = "TEXT")
    private String attributes; // JSON string for dynamic configuration (opening hours, etc.)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private UserEntity user;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<AddressEntity> addresses = new java.util.ArrayList<>();
}
