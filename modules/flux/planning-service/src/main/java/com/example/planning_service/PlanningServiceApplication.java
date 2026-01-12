package com.example.planning_service;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
@ComponentScan(basePackages = {"com.example.planning_service", "com.example.danxils_commons"})
public class PlanningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanningServiceApplication.class, args);
    }

}