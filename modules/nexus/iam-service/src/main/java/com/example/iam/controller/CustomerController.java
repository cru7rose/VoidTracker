package com.example.iam.controller;

import com.example.iam.dto.CustomerResponseDto;
import com.example.iam.dto.UpdateCustomerRequestDto;
import com.example.iam.entity.CustomerEntity;
import com.example.iam.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    public ResponseEntity<CustomerResponseDto> getMyProfile(@RequestHeader("X-User-Id") UUID userId) {
        // Assuming API Gateway passes X-User-Id
        CustomerEntity customer = customerService.getCustomerByUserId(userId);
        return ResponseEntity.ok(mapToDto(customer));
    }

    @PostMapping("/me")
    public ResponseEntity<CustomerResponseDto> createMyProfile(@RequestHeader("X-User-Id") UUID userId,
            @RequestBody UpdateCustomerRequestDto request) {
        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null");
        CustomerEntity customer = customerService.createCustomerProfile(userId, request);
        return ResponseEntity.ok(mapToDto(customer));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponseDto> updateMyProfile(@RequestHeader("X-User-Id") UUID userId,
            @RequestBody UpdateCustomerRequestDto request) {
        CustomerEntity customer = customerService.updateCustomerProfile(userId, request);
        return ResponseEntity.ok(mapToDto(customer));
    }

    // Admin endpoints
    @GetMapping("/users/{userId}")
    public ResponseEntity<CustomerResponseDto> getCustomerProfile(@PathVariable UUID userId) {
        CustomerEntity customer = customerService.getCustomerByUserId(userId);
        return ResponseEntity.ok(mapToDto(customer));
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<CustomerResponseDto> createCustomerProfile(@PathVariable UUID userId,
            @RequestBody UpdateCustomerRequestDto request) {
        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null");
        CustomerEntity customer = customerService.createCustomerProfile(userId, request);
        return ResponseEntity.ok(mapToDto(customer));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<CustomerResponseDto> updateCustomerProfile(@PathVariable UUID userId,
            @RequestBody UpdateCustomerRequestDto request) {
        CustomerEntity customer = customerService.updateCustomerProfile(userId, request);
        return ResponseEntity.ok(mapToDto(customer));
    }

    @PostMapping("/users/{userId}/addresses")
    public ResponseEntity<com.example.iam.dto.AddressDto> addAddress(@PathVariable UUID userId,
            @RequestBody com.example.iam.dto.AddressDto addressDto) {
        com.example.iam.entity.AddressEntity address = customerService.addAddress(userId, addressDto);
        return ResponseEntity.ok(mapAddressToDto(address));
    }

    @DeleteMapping("/users/{userId}/addresses/{addressId}")
    public ResponseEntity<Void> removeAddress(@PathVariable UUID userId, @PathVariable UUID addressId) {
        if (userId == null || addressId == null)
            throw new IllegalArgumentException("IDs cannot be null");
        customerService.removeAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    private CustomerResponseDto mapToDto(CustomerEntity entity) {
        CustomerResponseDto dto = new CustomerResponseDto();
        dto.setId(entity.getId());
        dto.setLegacyId(entity.getLegacyId());
        dto.setName(entity.getName());
        dto.setContactInfo(entity.getContactInfo());
        dto.setAttributes(entity.getAttributes());
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getUserId().toString());
        }
        if (entity.getAddresses() != null) {
            dto.setAddresses(entity.getAddresses().stream()
                    .map(this::mapAddressToDto)
                    .collect(java.util.stream.Collectors.toList()));
        }
        return dto;
    }

    private com.example.iam.dto.AddressDto mapAddressToDto(com.example.iam.entity.AddressEntity entity) {
        com.example.iam.dto.AddressDto dto = new com.example.iam.dto.AddressDto();
        dto.setId(entity.getId());
        dto.setStreet(entity.getStreet());
        dto.setHouseNumber(entity.getHouseNumber());
        dto.setPostalCode(entity.getPostalCode());
        dto.setCity(entity.getCity());
        dto.setCountry(entity.getCountry());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setType(entity.getType());
        return dto;
    }
}
