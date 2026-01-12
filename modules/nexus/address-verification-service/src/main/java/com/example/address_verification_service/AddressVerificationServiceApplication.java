package com.example.address_verification_service;
import com.example.address_verification_service.config.GoogleMapsApiProperties;
import com.example.address_verification_service.config.HereApiProperties;
import com.example.address_verification_service.config.NominatimApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({HereApiProperties.class, NominatimApiProperties.class, GoogleMapsApiProperties.class})
@ComponentScan(basePackages = {"com.example.address_verification_service", "com.example.danxils_commons"})
public class AddressVerificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AddressVerificationServiceApplication.class, args);
    }
}