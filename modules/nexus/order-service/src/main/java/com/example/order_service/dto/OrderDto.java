package com.example.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.danxils_commons.dto.ePoDDto;

import java.util.List;

@Data
@NoArgsConstructor
public class OrderDto {
    private String orderId;
    @NotBlank
    private String status;
    @NotBlank
    private String priority;
    @NotNull
    private PickupPointDto pickup;
    @NotNull
    private DeliveryPointDto delivery;
    @NotNull
    private PackageDto aPackage;
    @NotNull
    private TimestampsDto timestamps;
    private String assignedDriver;
    private List<ePoDDto> epod;

    @Data
    public static class PickupPointDto {
        private String customer;
        private String alias;
        private String country;
        private Integer addressId;
        private String postalCode;
        private String city;
        private String street;
        private String streetNumber;
        private String name;
        private String attention;
        private String route;
        private String routePart;
        private String type;
        private String manifestDate;
        private String windowFrom;
        private String windowTo;
        private String mail;
        private String phone;
        private String note;
    }

    @Data
    public static class DeliveryPointDto {
        private String customer;
        private String alias;
        private String country;
        private Integer addressId;
        private String postalCode;
        private String city;
        private String street;
        private String streetNumber;
        private String name;
        private String attention;
        private String route;
        private String routePart;
        private String type;
        private String manifestDate;
        private String sla;
        private String windowFrom;
        private String windowTo;
        private String mail;
        private String phone;
        private String note;
    }

    @Data
    public static class PackageDto {
        private String barcode1;
        private String barcode2;
        private Integer colli;
        private Double weight;
        private Double volume;
        private Double routeDistance;
        private String serviceType;
        private PackageDimensionsDto packageDimensions;
        private String driverNote;
        private String invoiceNote;
        private Double price;
        private String currency;
        private Boolean adr;
    }

    @Data
    public static class PackageDimensionsDto {
        private Double length;
        private Double width;
        private Double height;
    }

    @Data
    public static class TimestampsDto {
        private String created;
        private String lastStatusChange;
    }
}
