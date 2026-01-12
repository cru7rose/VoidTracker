package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID; // Zaimportuj klasę UUID

@Entity
@Table(name = "iam_users")
@Data
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    // --- POPRAWKA: Zmieniono typ z String na UUID ---
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private boolean enabled = true;

    @Column(name = "legacy_id")
    private String legacyId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "global_status")
    private GlobalStatus globalStatus = GlobalStatus.ACTIVE;

    @Column(columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String preferences = "{}";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "iam_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles")
    @Enumerated(EnumType.STRING)
    private java.util.Set<UserRole> roles = new java.util.HashSet<>();

    // Deprecated single role field, keeping for backward compatibility if needed,
    // but main logic should use the set.
    @Enumerated(EnumType.STRING)
    @Column(name = "role", insertable = false, updatable = false)
    private UserRole legacyRole;

    public UserRole getRole() {
        return legacyRole;
    }

    public void setRole(UserRole role) {
        this.legacyRole = role;
        // Optionally sync to roles set
        if (role != null) {
            this.roles.add(role);
        }
    }

    public enum GlobalStatus {
        ACTIVE, LOCKED, MFA_REQUIRED
    }
}
