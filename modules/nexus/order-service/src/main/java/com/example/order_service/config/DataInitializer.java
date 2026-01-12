package com.example.order_service.config;

import com.example.danxils_commons.enums.OrderStatus;
import com.example.order_service.entity.*;
import com.example.order_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
// @Component // Temporarily disabled
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final ClientRepository clientRepository;
        private final OrderRepository orderRepository;

        @Override
        @Transactional
        public void run(String... args) throws Exception {
                // Seed logic with ClientEntity
        }
}
