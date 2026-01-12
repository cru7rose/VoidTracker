package com.example.iam.service;

import com.example.iam.dto.UpdateCustomerRequestDto;
import com.example.iam.entity.CustomerEntity;
import com.example.iam.entity.UserEntity;
import com.example.iam.repository.CustomerRepository;
import com.example.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final com.example.iam.repository.AddressRepository addressRepository;

    public CustomerEntity getCustomerByUserId(UUID userId) {
        return customerRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found for user: " + userId));
    }

    @Transactional
    public CustomerEntity createCustomerProfile(@org.springframework.lang.NonNull UUID userId,
            UpdateCustomerRequestDto request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (customerRepository.findByUserUserId(userId).isPresent()) {
            throw new RuntimeException("Customer profile already exists for user: " + userId);
        }

        CustomerEntity customer = new CustomerEntity();
        customer.setUser(user);
        customer.setName(request.getName());
        customer.setContactInfo(request.getContactInfo());
        customer.setLegacyId(request.getLegacyId());
        customer.setAttributes(request.getAttributes());

        return customerRepository.save(customer);
    }

    @Transactional
    @SuppressWarnings("null")
    public CustomerEntity updateCustomerProfile(UUID userId, UpdateCustomerRequestDto request) {
        CustomerEntity customer = getCustomerByUserId(userId);

        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        if (request.getContactInfo() != null) {
            customer.setContactInfo(request.getContactInfo());
        }
        if (request.getLegacyId() != null) {
            customer.setLegacyId(request.getLegacyId());
        }
        if (request.getAttributes() != null) {
            customer.setAttributes(request.getAttributes());
        }

        return customerRepository.save(customer);
    }

    @Transactional
    public com.example.iam.entity.AddressEntity addAddress(UUID userId, com.example.iam.dto.AddressDto addressDto) {
        CustomerEntity customer = getCustomerByUserId(userId);

        com.example.iam.entity.AddressEntity address = new com.example.iam.entity.AddressEntity();
        address.setCustomer(customer);
        address.setStreet(addressDto.getStreet());
        address.setHouseNumber(addressDto.getHouseNumber());
        address.setPostalCode(addressDto.getPostalCode());
        address.setCity(addressDto.getCity());
        address.setCountry(addressDto.getCountry());
        address.setLatitude(addressDto.getLatitude());
        address.setLongitude(addressDto.getLongitude());
        address.setType(addressDto.getType());

        return addressRepository.save(address);
    }

    @Transactional
    public void removeAddress(@org.springframework.lang.NonNull UUID userId,
            @org.springframework.lang.NonNull UUID addressId) {
        CustomerEntity customer = getCustomerByUserId(userId);
        com.example.iam.entity.AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));

        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Address does not belong to this customer");
        }

        addressRepository.delete(address);
    }
}
