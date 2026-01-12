// File: dashboard-service/src/main/java/com/example/dashboard_service/client/IamAppClient.java
package com.example.dashboard_service.client;

import com.example.dashboard_service.client.fallback.IamAppClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Nowy klient Feign do komunikacji z `iam-app`.
 * Definiuje metodę do wywołania nowego, batchowego endpointu
 * w celu pobrania danych o użytkownikach.
 */
@FeignClient(name = "iam-app", fallback = IamAppClientFallback.class)
public interface IamAppClient {

    // Definicje DTO są lokalne dla klienta, aby uniknąć ścisłego powiązania modułów.
    record UserDetailsDto(UUID userId, String username) {}

    @PostMapping("/api/internal/users/details")
    List<UserDetailsDto> getUserDetails(@RequestBody List<UUID> userIds);
}