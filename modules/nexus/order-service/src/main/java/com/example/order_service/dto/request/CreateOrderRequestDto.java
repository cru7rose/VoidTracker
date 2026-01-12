package com.example.order_service.dto.request;

import com.example.danxils_commons.dto.AddressDto;
import com.example.danxils_commons.dto.PackageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

/**
 * ARCHITEKTURA: DTO reprezentujący żądanie utworzenia nowego zlecenia,
 * przychodzące do publicznego API. Jego struktura jest zwalidowana
 * i stanowi ścisły kontrakt dla klientów API.
 */
public class CreateOrderRequestDto {

        @NotBlank(message = "Customer ID cannot be blank")
        private String customerId;

        private String priority;
        private String remark;

        @NotNull(message = "Pickup address cannot be null")
        @Valid
        private AddressDto pickupAddress;

        @NotNull(message = "Delivery address cannot be null")
        @Valid
        private DeliveryAddressDto deliveryAddress;

        @NotNull(message = "Package details cannot be null")
        @Valid
        private PackageDto packageDetails;

        private List<String> requiredServiceCodes;

        private String externalReference;
        private String injectionHubId;
        private String costCenter;
        private Instant pickupTimeFrom;
        private Instant pickupTimeTo;
        private Instant deliveryTimeFrom;
        private Instant deliveryTimeTo;
        private Boolean isTransfer;

        public CreateOrderRequestDto() {
        }

        public CreateOrderRequestDto(String customerId, String priority, String remark, AddressDto pickupAddress,
                        DeliveryAddressDto deliveryAddress, PackageDto packageDetails,
                        List<String> requiredServiceCodes, Instant pickupTimeFrom, Instant pickupTimeTo,
                        Instant deliveryTimeFrom, Instant deliveryTimeTo, Boolean isTransfer) {
                this.customerId = customerId;
                this.priority = priority;
                this.remark = remark;
                this.pickupAddress = pickupAddress;
                this.deliveryAddress = deliveryAddress;
                this.packageDetails = packageDetails;
                this.requiredServiceCodes = requiredServiceCodes;
                this.pickupTimeFrom = pickupTimeFrom;
                this.pickupTimeTo = pickupTimeTo;
                this.deliveryTimeFrom = deliveryTimeFrom;
                this.deliveryTimeTo = deliveryTimeTo;
                this.isTransfer = isTransfer;
        }

        // Getters and Setters
        public String getCustomerId() {
                return customerId;
        }

        public void setCustomerId(String customerId) {
                this.customerId = customerId;
        }

        public String getPriority() {
                return priority;
        }

        public void setPriority(String priority) {
                this.priority = priority;
        }

        public String getRemark() {
                return remark;
        }

        public void setRemark(String remark) {
                this.remark = remark;
        }

        public AddressDto getPickupAddress() {
                return pickupAddress;
        }

        public void setPickupAddress(AddressDto pickupAddress) {
                this.pickupAddress = pickupAddress;
        }

        public DeliveryAddressDto getDeliveryAddress() {
                return deliveryAddress;
        }

        public void setDeliveryAddress(DeliveryAddressDto deliveryAddress) {
                this.deliveryAddress = deliveryAddress;
        }

        public PackageDto getPackageDetails() {
                return packageDetails;
        }

        public void setPackageDetails(PackageDto packageDetails) {
                this.packageDetails = packageDetails;
        }

        public List<String> getRequiredServiceCodes() {
                return requiredServiceCodes;
        }

        public void setRequiredServiceCodes(List<String> requiredServiceCodes) {
                this.requiredServiceCodes = requiredServiceCodes;
        }

        public Instant getPickupTimeFrom() {
                return pickupTimeFrom;
        }

        public void setPickupTimeFrom(Instant pickupTimeFrom) {
                this.pickupTimeFrom = pickupTimeFrom;
        }

        public Instant getPickupTimeTo() {
                return pickupTimeTo;
        }

        public void setPickupTimeTo(Instant pickupTimeTo) {
                this.pickupTimeTo = pickupTimeTo;
        }

        public Instant getDeliveryTimeFrom() {
                return deliveryTimeFrom;
        }

        public void setDeliveryTimeFrom(Instant deliveryTimeFrom) {
                this.deliveryTimeFrom = deliveryTimeFrom;
        }

        public Instant getDeliveryTimeTo() {
                return deliveryTimeTo;
        }

        public void setDeliveryTimeTo(Instant deliveryTimeTo) {
                this.deliveryTimeTo = deliveryTimeTo;
        }

        // Getters and Setters for new fields
        public String getExternalReference() {
                return externalReference;
        }

        public void setExternalReference(String externalReference) {
                this.externalReference = externalReference;
        }

        public String getInjectionHubId() {
                return injectionHubId;
        }

        public void setInjectionHubId(String injectionHubId) {
                this.injectionHubId = injectionHubId;
        }

        public String getCostCenter() {
                return costCenter;
        }

        public void setCostCenter(String costCenter) {
                this.costCenter = costCenter;
        }

        public Boolean getIsTransfer() {
                return isTransfer;
        }

        public void setIsTransfer(Boolean isTransfer) {
                this.isTransfer = isTransfer;
        }

        /**
         * ARCHITEKTURA: Zagnieżdżony DTO specyficzny dla adresu dostawy w żądaniu,
         * aby umożliwić przekazanie wymaganego czasu dostawy (SLA).
         */
        public static class DeliveryAddressDto {
                private String customerName;
                private String attention;
                private String street;
                private String streetNumber;
                private String apartment;
                private String postalCode;
                private String city;
                private String country;
                private String mail;
                private String phone;
                private String note;
                private Double lat;
                private Double lon;
                private Instant sla;

                public DeliveryAddressDto() {
                }

                public DeliveryAddressDto(String customerName, String attention, String street, String streetNumber,
                                String postalCode, String city, String country, String mail, String phone, String note,
                                Double lat, Double lon, Instant sla) {
                        this.customerName = customerName;
                        this.attention = attention;
                        this.street = street;
                        this.streetNumber = streetNumber;
                        this.postalCode = postalCode;
                        this.city = city;
                        this.country = country;
                        this.mail = mail;
                        this.phone = phone;
                        this.note = note;
                        this.lat = lat;
                        this.lon = lon;
                        this.sla = sla;
                }

                public String getCustomerName() {
                        return customerName;
                }

                public void setCustomerName(String customerName) {
                        this.customerName = customerName;
                }

                public String getAttention() {
                        return attention;
                }

                public void setAttention(String attention) {
                        this.attention = attention;
                }

                public String getStreet() {
                        return street;
                }

                public void setStreet(String street) {
                        this.street = street;
                }

                public String getStreetNumber() {
                        return streetNumber;
                }

                public void setStreetNumber(String streetNumber) {
                        this.streetNumber = streetNumber;
                }

                public String getApartment() {
                        return apartment;
                }

                public void setApartment(String apartment) {
                        this.apartment = apartment;
                }

                public String getPostalCode() {
                        return postalCode;
                }

                public void setPostalCode(String postalCode) {
                        this.postalCode = postalCode;
                }

                public String getCity() {
                        return city;
                }

                public void setCity(String city) {
                        this.city = city;
                }

                public String getCountry() {
                        return country;
                }

                public void setCountry(String country) {
                        this.country = country;
                }

                public String getMail() {
                        return mail;
                }

                public void setMail(String mail) {
                        this.mail = mail;
                }

                public String getPhone() {
                        return phone;
                }

                public void setPhone(String phone) {
                        this.phone = phone;
                }

                public String getNote() {
                        return note;
                }

                public void setNote(String note) {
                        this.note = note;
                }

                public Double getLat() {
                        return lat;
                }

                public void setLat(Double lat) {
                        this.lat = lat;
                }

                public Double getLon() {
                        return lon;
                }

                public void setLon(Double lon) {
                        this.lon = lon;
                }

                public Instant getSla() {
                        return sla;
                }

                public void setSla(Instant sla) {
                        this.sla = sla;
                }
        }
}