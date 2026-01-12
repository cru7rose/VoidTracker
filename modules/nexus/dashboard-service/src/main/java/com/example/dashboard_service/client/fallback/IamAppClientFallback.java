package com.example.dashboard_service.client.fallback;

import com.example.dashboard_service.client.IamAppClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Implementacja logiki zastępczej (fallback) dla klienta IamAppClient.
 * Zapewnia kontrolowaną degradację usługi w przypadku niedostępności `iam-app`.
 */
@Slf4j
@Component
public class IamAppClientFallback implements IamAppClient {

    @Override
    public List<UserDetailsDto> getUserDetails(List<UUID> userIds) {
        log.error("CIRCUIT BREAKER: Fallback for getUserDetails triggered. Iam-app is unavailable. Returning empty list.");
        return Collections.emptyList();
    }
}