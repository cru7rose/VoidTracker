package com.example.order_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

// ... (komentarze klasy)
@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    private String nominatimUrl;
    private String n8nUrl;
    private final Kafka kafka = new Kafka();

    public String getNominatimUrl() {
        return nominatimUrl;
    }

    public void setNominatimUrl(String nominatimUrl) {
        this.nominatimUrl = nominatimUrl;
    }

    public String getN8nUrl() {
        return n8nUrl;
    }

    public void setN8nUrl(String n8nUrl) {
        this.n8nUrl = n8nUrl;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public static class Kafka {
        private final Topics topics = new Topics();

        public Topics getTopics() {
            return topics;
        }

        public static class Topics {
            private String ordersCreated;
            private String ordersAssigned;
            private String ordersStatusChanged;
            private String driverLocationUpdated;
            private String routePlanned;

            public String getOrdersCreated() {
                return ordersCreated;
            }

            public void setOrdersCreated(String ordersCreated) {
                this.ordersCreated = ordersCreated;
            }

            public String getOrdersAssigned() {
                return ordersAssigned;
            }

            public void setOrdersAssigned(String ordersAssigned) {
                this.ordersAssigned = ordersAssigned;
            }

            public String getOrdersStatusChanged() {
                return ordersStatusChanged;
            }

            public void setOrdersStatusChanged(String ordersStatusChanged) {
                this.ordersStatusChanged = ordersStatusChanged;
            }

            public String getDriverLocationUpdated() {
                return driverLocationUpdated;
            }

            public void setDriverLocationUpdated(String driverLocationUpdated) {
                this.driverLocationUpdated = driverLocationUpdated;
            }

            public String getRoutePlanned() {
                return routePlanned;
            }

            public void setRoutePlanned(String routePlanned) {
                this.routePlanned = routePlanned;
            }
        }
    }
}