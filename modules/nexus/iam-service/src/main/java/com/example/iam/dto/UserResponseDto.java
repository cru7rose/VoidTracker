package com.example.iam.dto;

import lombok.Data;
import java.util.Set;

/**
 * ARCHITEKTURA: DTO używane w odpowiedziach API dotyczących użytkowników.
 * Celowo nie zawiera pola 'password', aby zapobiec wyciekowi zahaszowanego
 * hasła.
 */
@Data
public class UserResponseDto {
    private String userId;
    private String username;
    private String email;
    private boolean enabled;
    private String legacyId;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private Set<String> roles;
    private java.util.Map<String, Object> organizationConfig; // For department restrictions
}