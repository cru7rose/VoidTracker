package com.example.order_service.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class WeatherService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WeatherService.class);

    private final Random random = new Random();

    public Double getCurrentTemperature(Double lat, Double lon) {
        // Mock implementation: Returns a random temperature between 20.0 and 35.0
        // degrees Celsius
        // In a real implementation, this would call an external Weather API (e.g.,
        // OpenWeatherMap)
        double temp = 20.0 + (35.0 - 20.0) * random.nextDouble();
        log.debug("Mock WeatherService: Temperature at [{}, {}] is {}°C", lat, lon, String.format("%.1f", temp));
        return temp;
    }
}
