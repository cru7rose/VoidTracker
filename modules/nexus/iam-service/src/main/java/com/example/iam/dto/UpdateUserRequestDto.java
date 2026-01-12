package com.example.iam.dto;

import lombok.Data;

import java.util.Set;

/**
 * ARCHITEKTURA: DTO używane jako ciało żądania dla operacji aktualizacji
 * danych użytkownika. Zawiera pola, które administrator może modyfikować.
 * Celowo nie zawiera pola 'password', aby aktualizacja hasła odbywała się
 * poprzez osobny, dedykowany proces.
 */
@Data
public class UpdateUserRequestDto {
    private String username;
    private String email;
    private Set<String> roles; // Accept role names as strings
    private String legacyId;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private Boolean enabled; // Changed to Boolean to allow null
    private String preferences;
}