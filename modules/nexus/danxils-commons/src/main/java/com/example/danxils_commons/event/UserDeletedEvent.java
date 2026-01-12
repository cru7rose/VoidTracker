package com.example.danxils_commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * ARCHITEKTURA: Kanoniczne zdarzenie domenowe informujące o usunięciu
 * użytkownika z systemu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletedEvent {
    private UUID eventId;
    private Instant timestamp;
    private String performedBy;
    private UUID userId;
    private String email;
}