package com.example.order_service.model;

import com.example.danxils_commons.dto.AddressDto;
import com.example.danxils_commons.dto.PackageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank
    private String customerId;

    private String priority = "NORMAL";

    @NotNull @Valid
    private AddressDto pickupAddress;

    @NotNull @Valid
    private AddressDto deliveryAddress;

    @NotNull @Valid
    private PackageDto packageDetails;
}