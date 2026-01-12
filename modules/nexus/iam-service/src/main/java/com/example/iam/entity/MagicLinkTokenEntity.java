package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Token do logowania "Magic Link".
 * Umożliwia kierowcom logowanie bez hasła poprzez kliknięcie w link.
 */
@Entity
@Table(name = "iam_magic_link_tokens")
@Data
@NoArgsConstructor
public class MagicLinkTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean isUsed = false;
}
