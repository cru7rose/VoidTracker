package com.example.danxils_commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kanoniczne zdarzenie domenowe informujące o modyfikacji
 * danych użytkownika. Zawiera stan przed i po zmianie, co jest
 * kluczowe dla szczegółowego audytu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {
    private UUID eventId;
    private Instant timestamp;
    private String performedBy;
    private UUID userId;
    private UserPayload before;
    private UserPayload after;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPayload {
        private String username;
        private String email;
        private String fullName;
        private boolean enabled;
        private Set<String> roles;
        private String avatarUrl;
        private String bio;
    }
}