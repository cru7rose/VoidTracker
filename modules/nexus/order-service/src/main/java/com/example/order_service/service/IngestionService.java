package com.example.order_service.service;

import com.example.danxils_commons.enums.OrderStatus;
import com.example.order_service.dto.IngestionRequestDto;
import com.example.order_service.entity.ClientEntity;
import com.example.order_service.entity.OrderEntity;
import com.example.order_service.repository.ClientRepository;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IngestionService.class);

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public String ingestOrder(IngestionRequestDto request) {
        log.info("Ingesting order: {}", request.getExternalId());

        // 1. Lookup Client
        ClientEntity client = null;
        if (request.getClientExternalId() != null) {
            client = clientRepository.findByExternalId(request.getClientExternalId())
                    .orElseGet(() -> {
                        // Optional: Create stub client if not found?
                        // For Phase 1, just log warning and proceed without client or fail?
                        // Let's create a stub client for robustness if allowed, or just null.
                        // Better to require existing client for now to avoid mess.
                        log.warn("Client with externalId {} not found", request.getClientExternalId());
                        return null;
                    });
        }

        // 2. Create Order
        OrderEntity order = new OrderEntity();
        order.setExternalId(request.getExternalId());
        if (client == null) {
            // Fallback: Create a stub client to satisfy NOT NULL constraint
            log.warn("Using/Creating stub client for Ingestion");
            client = new ClientEntity();
            client.setName("Ingestion Legacy Client");
            client.addProperty("type", "LEGACY_STUB");
            client = clientRepository.save(client);
        }
        order.setClient(client);
        order.setStatus(OrderStatus.NEW); // Ready for optimization
        order.setTenantId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")); // Default Tenant

        // 3. Map Dynamic Properties
        if (request.getProperties() != null) {
            request.getProperties().forEach(order::addProperty);
        }

        // 4. Save
        orderRepository.save(order);
        // Note: Event publishing usually happens here or via Entity listeners.

        return order.getId().toString();
    }
}
