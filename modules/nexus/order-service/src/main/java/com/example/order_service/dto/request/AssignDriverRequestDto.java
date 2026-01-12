package com.example.order_service.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * ARCHITEKTURA: DTO dla żądania przypisania kierowcy do zlecenia.
 * Definiuje prosty, ale ściśle określony kontrakt dla tej operacji biznesowej,
 * zawierający identyfikator przypisywanego kierowcy.
 */
public class AssignDriverRequestDto {

        @NotBlank(message = "Driver ID cannot be blank")
        private String driverId;

        public AssignDriverRequestDto() {
        }

        public AssignDriverRequestDto(String driverId) {
                this.driverId = driverId;
        }

        public String getDriverId() {
                return driverId;
        }

        public void setDriverId(String driverId) {
                this.driverId = driverId;
        }
}