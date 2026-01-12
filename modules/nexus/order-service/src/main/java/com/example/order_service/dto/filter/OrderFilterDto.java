// File: order-service/src/main/java/com/example/order_service/dto/filter/OrderFilterDto.java
package com.example.order_service.dto.filter;

import com.example.danxils_commons.enums.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * ARCHITEKTURA: Obiekt Transferu Danych (DTO) hermetyzujący parametry
 * filtrowania
 * dla zapytań o listę zleceń. Używany przez OrderController do przekazywania
 * kryteriów wyszukiwania do warstwy serwisowej.
 */
public class OrderFilterDto {
    private List<OrderStatus> statuses;
    private UUID customerId;
    private String driverId;
    private Instant dateFrom;
    private Instant dateTo;

    public OrderFilterDto() {
    }

    public OrderFilterDto(List<OrderStatus> statuses, UUID customerId, String driverId, Instant dateFrom,
            Instant dateTo) {
        this.statuses = statuses;
        this.customerId = customerId;
        this.driverId = driverId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public static OrderFilterDtoBuilder builder() {
        return new OrderFilterDtoBuilder();
    }

    public List<OrderStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<OrderStatus> statuses) {
        this.statuses = statuses;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public Instant getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Instant dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Instant getDateTo() {
        return dateTo;
    }

    public void setDateTo(Instant dateTo) {
        this.dateTo = dateTo;
    }

    public static class OrderFilterDtoBuilder {
        private List<OrderStatus> statuses;
        private UUID customerId;
        private String driverId;
        private Instant dateFrom;
        private Instant dateTo;

        OrderFilterDtoBuilder() {
        }

        public OrderFilterDtoBuilder statuses(List<OrderStatus> statuses) {
            this.statuses = statuses;
            return this;
        }

        public OrderFilterDtoBuilder customerId(UUID customerId) {
            this.customerId = customerId;
            return this;
        }

        public OrderFilterDtoBuilder driverId(String driverId) {
            this.driverId = driverId;
            return this;
        }

        public OrderFilterDtoBuilder dateFrom(Instant dateFrom) {
            this.dateFrom = dateFrom;
            return this;
        }

        public OrderFilterDtoBuilder dateTo(Instant dateTo) {
            this.dateTo = dateTo;
            return this;
        }

        public OrderFilterDto build() {
            return new OrderFilterDto(statuses, customerId, driverId, dateFrom, dateTo);
        }

        public String toString() {
            return "OrderFilterDto.OrderFilterDtoBuilder(statuses=" + this.statuses + ", customerId=" + this.customerId
                    + ", driverId=" + this.driverId + ", dateFrom=" + this.dateFrom + ", dateTo=" + this.dateTo + ")";
        }
    }
}