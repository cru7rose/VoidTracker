// File: danxils-commons/src/main/java/com/example/danxils_commons/dto/internal/OrderQueryRequestDto.java
package com.example.danxils_commons.dto;

import com.example.danxils_commons.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: DTO używane jako ciało żądania dla wewnętrznego endpointu
 * /api/internal/orders/query. Umożliwia innym mikroserwisom (np.
 * planning-service)
 * na bezpieczne i elastyczne odpytywanie o zlecenia spełniające złożone
 * kryteria.
 * ✅ FIX: Przeniesiono do danxils-commons jako współdzielony kontrakt.
 */
public class OrderQueryRequestDto {
        private List<OrderStatus> statuses;
        private List<UUID> customerIds;
        private List<String> postalCodePrefixes;
        private List<String> priorities;

        public OrderQueryRequestDto() {
        }

        public OrderQueryRequestDto(List<OrderStatus> statuses, List<UUID> customerIds, List<String> postalCodePrefixes,
                        List<String> priorities) {
                this.statuses = statuses;
                this.customerIds = customerIds;
                this.postalCodePrefixes = postalCodePrefixes;
                this.priorities = priorities;
        }

        public static OrderQueryRequestDtoBuilder builder() {
                return new OrderQueryRequestDtoBuilder();
        }

        public List<OrderStatus> getStatuses() {
                return statuses;
        }

        public void setStatuses(List<OrderStatus> statuses) {
                this.statuses = statuses;
        }

        public List<UUID> getCustomerIds() {
                return customerIds;
        }

        public void setCustomerIds(List<UUID> customerIds) {
                this.customerIds = customerIds;
        }

        public List<String> getPostalCodePrefixes() {
                return postalCodePrefixes;
        }

        public void setPostalCodePrefixes(List<String> postalCodePrefixes) {
                this.postalCodePrefixes = postalCodePrefixes;
        }

        public List<String> getPriorities() {
                return priorities;
        }

        public void setPriorities(List<String> priorities) {
                this.priorities = priorities;
        }

        public static class OrderQueryRequestDtoBuilder {
                private List<OrderStatus> statuses;
                private List<UUID> customerIds;
                private List<String> postalCodePrefixes;
                private List<String> priorities;

                OrderQueryRequestDtoBuilder() {
                }

                public OrderQueryRequestDtoBuilder statuses(List<OrderStatus> statuses) {
                        this.statuses = statuses;
                        return this;
                }

                public OrderQueryRequestDtoBuilder customerIds(List<UUID> customerIds) {
                        this.customerIds = customerIds;
                        return this;
                }

                public OrderQueryRequestDtoBuilder postalCodePrefixes(List<String> postalCodePrefixes) {
                        this.postalCodePrefixes = postalCodePrefixes;
                        return this;
                }

                public OrderQueryRequestDtoBuilder priorities(List<String> priorities) {
                        this.priorities = priorities;
                        return this;
                }

                public OrderQueryRequestDto build() {
                        return new OrderQueryRequestDto(statuses, customerIds, postalCodePrefixes, priorities);
                }

                public String toString() {
                        return "OrderQueryRequestDto.OrderQueryRequestDtoBuilder(statuses=" + this.statuses
                                        + ", customerIds=" + this.customerIds + ", postalCodePrefixes="
                                        + this.postalCodePrefixes + ", priorities=" + this.priorities + ")";
                }
        }
}