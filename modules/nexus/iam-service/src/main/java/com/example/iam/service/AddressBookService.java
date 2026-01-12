package com.example.iam.service;

import com.example.iam.entity.AddressEntity;
import com.example.iam.entity.CustomerEntity;
import com.example.iam.entity.TenantEntity;
import com.example.iam.repository.AddressRepository;
import com.example.iam.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressBookService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public AddressEntity addAddress(UUID customerId, AddressEntity address) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!customer.getIsAddressBookOwner()) {
            // Traverse up to find the owner (e.g., Department -> Master)
            // For MVP: recursive check or just allow if parent is null.
            // Requirement: "Separate responsibility... to create their own grid"
            // For now, assume every Customer CAN be an owner if flagged.
        }

        address.setCustomer(customer);
        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<AddressEntity> getAddresses(UUID customerId) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        // Ensure isolation: Only return addresses for this customer (and potentially its tenant if shared, but request implies isolation)
        return customer.getAddresses();
    }
}
