package com.example.order_service.controller;

import com.example.order_service.dto.HarmonogramScheduleDto;
import com.example.order_service.entity.ClientEntity;
import com.example.order_service.entity.CustomerHarmonogramEntity;
import com.example.order_service.repository.ClientRepository;
import com.example.order_service.repository.CustomerHarmonogramRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/harmonograms")
@RequiredArgsConstructor
public class CustomerHarmonogramController {

        private final CustomerHarmonogramRepository harmonogramRepository;
        private final ClientRepository clientRepository;

        @GetMapping("/{clientId}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_DISPATCHER')")
        public ResponseEntity<CustomerHarmonogramEntity> getHarmonogram(@PathVariable UUID clientId) {
                return harmonogramRepository.findByClient_Id(clientId)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @PostMapping("/{clientId}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public ResponseEntity<CustomerHarmonogramEntity> createOrUpdateHarmonogram(
                        @PathVariable UUID clientId,
                        @RequestBody CustomerHarmonogramEntity request) {

                ClientEntity client = clientRepository.findById(clientId)
                                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));

                CustomerHarmonogramEntity harmonogram = harmonogramRepository.findByClient_Id(clientId)
                                .orElse(new CustomerHarmonogramEntity());

                harmonogram.setClient(client);
                harmonogram.setAlias(request.getAlias());
                harmonogram.setSchedule(request.getSchedule());
                harmonogram.setNonDeliveryDates(request.getNonDeliveryDates());

                CustomerHarmonogramEntity saved = harmonogramRepository.save(harmonogram);
                return ResponseEntity.ok(saved);
        }
}
