package com.example.iam.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AddressDto {
    private UUID id;
    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private String type;
}
