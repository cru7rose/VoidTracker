package com.example.crm_service.service;

import com.example.crm_service.dto.ClientPreferenceDto;
import com.example.crm_service.entity.ClientProfileEntity;
import com.example.crm_service.repository.ClientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientPreferenceService {

    private final ClientProfileRepository repository;

    @Transactional(readOnly = true)
    public ClientPreferenceDto getPreferences(UUID clientId) {
        ClientProfileEntity entity = repository.findByClientId(clientId)
                .orElse(new ClientProfileEntity()); // Return empty/default if not found, or could throw exception

        // If it's a new empty entity, we should probably set the ID
        if (entity.getClientId() == null) {
            entity.setClientId(clientId);
        }

        ClientPreferenceDto dto = new ClientPreferenceDto();
        dto.setClientId(entity.getClientId());
        dto.setNotificationEmail(entity.getNotificationEmail());
        dto.setPreferredCarrier(entity.getPreferredCarrier());
        dto.setDefaultServiceLevel(entity.getDefaultServiceLevel());
        dto.setRampHours(entity.getRampHours());

        return dto;
    }

    @Transactional
    public ClientPreferenceDto updatePreferences(UUID clientId, ClientPreferenceDto dto) {
        ClientProfileEntity entity = repository.findByClientId(clientId)
                .orElseGet(() -> {
                    ClientProfileEntity newEntity = new ClientProfileEntity();
                    newEntity.setClientId(clientId);
                    return newEntity;
                });

        if (dto.getNotificationEmail() != null)
            entity.setNotificationEmail(dto.getNotificationEmail());
        if (dto.getPreferredCarrier() != null)
            entity.setPreferredCarrier(dto.getPreferredCarrier());
        if (dto.getDefaultServiceLevel() != null)
            entity.setDefaultServiceLevel(dto.getDefaultServiceLevel());
        if (dto.getRampHours() != null)
            entity.setRampHours(dto.getRampHours());

        ClientProfileEntity saved = repository.save(entity);

        return getPreferences(saved.getClientId());
    }
}
