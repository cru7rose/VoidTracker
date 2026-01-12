package com.example.danxils_commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kanoniczne zdarzenie domenowe informujące o zainicjowaniu
 * procesu tworzenia nowego użytkownika w systemie.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private UUID eventId;
    private Instant timestamp;
    private String performedBy;
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private String avatarUrl;
    private String bio;
}