package com.example.order_service.repository;

import com.example.order_service.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {

    /**
     * Wyszukuje adres na podstawie kombinacji kluczowych pól oraz ID właściciela.
     * Używane w OrderService do deduplikacji adresów w książce adresowej klienta.
     *
     * @param customerId   ID klienta (właściciela adresu)
     * @param street       Nazwa ulicy
     * @param streetNumber Numer budynku
     * @param postalCode   Kod pocztowy
     * @param city         Miasto
     * @return Optional z encją adresu, jeśli zostanie znaleziony.
     */
    Optional<AddressEntity> findByOwnerClient_IdAndStreetAndStreetNumberAndPostalCodeAndCity(
            UUID ownerClientId, String street, String streetNumber, String postalCode, String city);
}