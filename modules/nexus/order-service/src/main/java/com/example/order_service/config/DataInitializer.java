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
                // Skip during build/test - only run when actually starting the application
                if (isBuildOrTestPhase(args)) {
                        log.debug("Skipping data initialization during build/test phase");
                        return;
                }
                // Seed logic with ClientEntity
        }

        /**
         * Check if we're in a build or test phase to prevent database connections during Maven build
         */
        private boolean isBuildOrTestPhase(String... args) {
                // Check if running during Maven build (common indicators)
                String classpath = System.getProperty("java.class.path", "");
                return classpath.contains("maven") || 
                       classpath.contains("surefire") ||
                       System.getProperty("maven.test.skip") != null ||
                       System.getProperty("skipTests") != null ||
                       System.getProperty("spring.profiles.active", "").contains("test");
        }
}
