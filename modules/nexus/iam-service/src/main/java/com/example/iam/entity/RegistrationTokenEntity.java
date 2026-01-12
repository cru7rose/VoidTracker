package com.example.iam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Encja reprezentująca jednorazowy token rejestracyjny powiązany z nowo tworzonym kontem użytkownika.
 * Token posiada określony czas ważności i jest usuwany po pomyślnym zakończeniu rejestracji.
 * Zapewnia to bezpieczny, dwuetapowy proces tworzenia konta.
 */
@Entity
@Table(name = "iam_registration_tokens")
@Data
@NoArgsConstructor
public class RegistrationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tokenId;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private Instant expiryDate;

    public RegistrationTokenEntity(UserEntity user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        // Domyślna ważność tokenu ustawiona na 24 godziny
        this.expiryDate = Instant.now().plusSeconds(86400);
    }
}