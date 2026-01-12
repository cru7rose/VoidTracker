package com.example.driver_app_service;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.example.driver_app_service", "com.example.danxils_commons"})
public class DriverAppServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriverAppServiceApplication.class, args);
    }

}