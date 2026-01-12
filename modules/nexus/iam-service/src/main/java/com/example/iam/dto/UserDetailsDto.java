// File: iam-app/src/main/java/com/example/iam/dto/UserDetailsDto.java
package com.example.iam.dto;

import lombok.Data;
import java.util.UUID;

/**
 * ARCHITEKTURA: Lekkie DTO do wewnętrznej komunikacji, zawierające
 * tylko niezbędne, publiczne dane o użytkowniku na potrzeby innych serwisów.
 */
@Data
public class UserDetailsDto {
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
}