package com.example.iam.repository;

import com.example.iam.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
    List<AddressEntity> findByCustomer_Id(UUID customerId);
}
