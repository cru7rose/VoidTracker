package com.example.admin_panel_service;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.example.admin_panel_service", "com.example.danxils_commons"})
public class AdminPanelServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminPanelServiceApplication.class, args);
    }
}