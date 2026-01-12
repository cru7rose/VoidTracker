package com.example.order_service.repository;

import com.example.order_service.entity.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, UUID> {
    Optional<PackageEntity> findByBarcode1(String barcode1);
}