package com.example.admin_panel_service.service;

import com.example.admin_panel_service.client.OrderServiceClient;
import com.example.danxils_commons.dto.AdditionalServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdditionalServiceAdminService {

    private final OrderServiceClient orderServiceClient;

    public List<AdditionalServiceDto> getAllServices() {
        return orderServiceClient.getAllServices();
    }

    public AdditionalServiceDto createService(AdditionalServiceDto serviceDto) {
        return orderServiceClient.createService(serviceDto);
    }

    public AdditionalServiceDto getServiceById(UUID serviceId) {
        return orderServiceClient.getServiceById(serviceId);
    }

    public AdditionalServiceDto updateService(UUID serviceId, AdditionalServiceDto serviceDto) {
        return orderServiceClient.updateService(serviceId, serviceDto);
    }

    public void deleteService(UUID serviceId) {
        orderServiceClient.deleteService(serviceId);
    }
}