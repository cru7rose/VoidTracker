package com.example.billing_service.controller;

import com.example.billing_service.entity.RateCardEntity;
import com.example.billing_service.repository.RateCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing/rate-cards")
@RequiredArgsConstructor
public class RateCardController {

    private final RateCardRepository rateCardRepository;

    @GetMapping
    public List<RateCardEntity> getAll() {
        return rateCardRepository.findAll();
    }

    @PostMapping
    public RateCardEntity create(@RequestBody RateCardEntity rateCard) {
        return rateCardRepository.save(rateCard);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RateCardEntity> getById(@PathVariable UUID id) {
        return rateCardRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
