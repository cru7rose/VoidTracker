// File: planning-service/src/main/java/com/example/planning_service/config/AppProperties.java
package com.example.planning_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app")
@Data
@Validated
public class AppProperties {

    private final Kafka kafka = new Kafka();
    private final Vrp vrp = new Vrp();
    private final Depot depot = new Depot();

    @Data
    public static class Kafka {
        private final Topics topics = new Topics();

        @Data
        public static class Topics {
            private String ordersCreated;
            private String ordersAssigned;
            private String ordersStatusChanged;
            private String driverLocationUpdated;
            private String routePlanned;
            private String ordersCreatedDlt;
        }
    }

    @Data
    public static class Vrp {
        private int maxIterations = 1000;
        private int timeLimitSeconds = 30;
    }

    @Data
    public static class Depot {
        private double latitude = 52.237049;
        private double longitude = 21.017532;
        private String name = "Main Warehouse";
    }
}