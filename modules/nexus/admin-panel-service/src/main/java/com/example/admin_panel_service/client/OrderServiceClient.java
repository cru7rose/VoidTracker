package com.example.admin_panel_service.client;

import com.example.danxils_commons.dto.AdditionalServiceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/api/admin/services")
    List<AdditionalServiceDto> getAllServices();

    @PostMapping("/api/admin/services")
    AdditionalServiceDto createService(@RequestBody AdditionalServiceDto serviceDto);

    @GetMapping("/api/admin/services/{serviceId}")
    AdditionalServiceDto getServiceById(@PathVariable("serviceId") UUID serviceId);

    @PutMapping("/api/admin/services/{serviceId}")
    AdditionalServiceDto updateService(@PathVariable("serviceId") UUID serviceId, @RequestBody AdditionalServiceDto serviceDto);

    @DeleteMapping("/api/admin/services/{serviceId}")
    void deleteService(@PathVariable("serviceId") UUID serviceId);
}