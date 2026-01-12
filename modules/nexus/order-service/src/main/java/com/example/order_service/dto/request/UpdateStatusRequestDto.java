package com.example.order_service.dto.request;

import com.example.danxils_commons.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * ARCHITEKTURA: DTO dla żądania zmiany statusu zlecenia.
 * Definiuje kontrakt dla wewnętrznych i zewnętrznych operacji zmiany statusu.
 */
public class UpdateStatusRequestDto {

        @NotNull(message = "New status cannot be null")
        private OrderStatus newStatus;

        public UpdateStatusRequestDto() {
        }

        public UpdateStatusRequestDto(OrderStatus newStatus) {
                this.newStatus = newStatus;
        }

        public OrderStatus getNewStatus() {
                return newStatus;
        }

        public void setNewStatus(OrderStatus newStatus) {
                this.newStatus = newStatus;
        }
}