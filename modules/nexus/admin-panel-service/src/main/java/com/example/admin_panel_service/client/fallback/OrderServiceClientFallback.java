package com.example.admin_panel_service.client.fallback;

import com.example.admin_panel_service.client.OrderServiceClient;
import com.example.danxils_commons.dto.AdditionalServiceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Implementacja logiki zastępczej (fallback) dla OrderServiceClient.
 * Sygnatury metod zostały zaktualizowane, aby były w 100% zgodne z oczyszczonym
 * interfejsem klienta Feign.
 */
@Slf4j
@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public List<AdditionalServiceDto> getAllServices() {
        log.error("CIRCUIT BREAKER: Fallback for getAllServices triggered. Order-service is unavailable. Returning empty list.");
        return Collections.emptyList();
    }

    @Override
    public AdditionalServiceDto createService(AdditionalServiceDto serviceDto) {
        log.error("CIRCUIT BREAKER: Fallback for createService triggered. Order-service is unavailable.");
        throw new IllegalStateException("Order service is currently unavailable. Please try again later.");
    }

    @Override
    public AdditionalServiceDto getServiceById(UUID serviceId) {
        log.error("CIRCUIT BREAKER: Fallback for getServiceById triggered for service {}. Order-service is unavailable.", serviceId);
        return null;
    }

    @Override
    public AdditionalServiceDto updateService(UUID serviceId, AdditionalServiceDto serviceDto) {
        log.error("CIRCUIT BREAKER: Fallback for updateService triggered for service {}. Order-service is unavailable.", serviceId);
        throw new IllegalStateException("Order service is currently unavailable. Please try again later.");
    }

    @Override
    public void deleteService(UUID serviceId) {
        log.error("CIRCUIT BREAKER: Fallback for deleteService triggered for service {}. Order-service is unavailable.", serviceId);
        throw new IllegalStateException("Order service is currently unavailable. Please try again later.");
    }
}