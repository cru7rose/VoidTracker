package com.example.order_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * ARCHITEKTURA: DTO dla żądania potwierdzenia odbioru/załadunku.
 * Definiuje prosty kontrakt, zawierający listę zeskanowanych przez kierowcę
 * kodów kreskowych.
 */
public class ConfirmPickupRequestDto {

        @NotEmpty(message = "List of scanned barcodes cannot be empty.")
        private List<String> scannedBarcodes;

        public ConfirmPickupRequestDto() {
        }

        public ConfirmPickupRequestDto(List<String> scannedBarcodes) {
                this.scannedBarcodes = scannedBarcodes;
        }

        public List<String> getScannedBarcodes() {
                return scannedBarcodes;
        }

        public void setScannedBarcodes(List<String> scannedBarcodes) {
                this.scannedBarcodes = scannedBarcodes;
        }
}