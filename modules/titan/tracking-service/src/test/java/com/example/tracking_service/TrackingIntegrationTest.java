package com.example.tracking_service;

import com.example.danxils_commons.event.DriverLocationUpdatedEvent;
import com.example.tracking_service.dto.LocationUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TrackingIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    private static final SecretKey TEST_SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final String TEST_JWT_SECRET_STRING = java.util.Base64.getEncoder().encodeToString(TEST_SECRET_KEY.getEncoded());

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("jwt.secret.key", () -> TEST_JWT_SECRET_STRING);
        registry.add("app.kafka.topics.driver-location-updated", () -> "driver.location.updated.test");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.driver-location-updated}")
    private String driverLocationUpdatedTopic;

    @Captor
    ArgumentCaptor<DriverLocationUpdatedEvent> eventCaptor;

    @Test
    void shouldAcceptLocationUpdateAndPublishEventToKafka() throws Exception {
        // --- KROK 1: Przygotowanie danych i tokenu JWT ---
        String driverUsername = "test_driver";
        String driverJwt = generateTestJwt(driverUsername, "ROLE_DRIVER");

        LocationUpdateRequest locationUpdate = new LocationUpdateRequest();
        locationUpdate.setOrderId(UUID.randomUUID());
        locationUpdate.setLatitude(52.237049);
        locationUpdate.setLongitude(21.017532);

        // --- KROK 2: AKCJA - Wywołanie endpointu API ---
        mockMvc.perform(post("/api/tracking/location")
                        .header("Authorization", "Bearer " + driverJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locationUpdate)))
                .andExpect(status().isAccepted());

        // --- KROK 3: WERYFIKACJA (ASYNC) - Sprawdzenie, czy KafkaTemplate.send() zostało wywołane ---
        verify(kafkaTemplate, timeout(5000)).send(
                org.mockito.ArgumentMatchers.eq(driverLocationUpdatedTopic),
                org.mockito.ArgumentMatchers.anyString(),
                eventCaptor.capture()
        );

        DriverLocationUpdatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent).isNotNull();
        assertThat(publishedEvent.getDriverId()).isEqualTo(driverUsername);
        assertThat(publishedEvent.getOrderId()).isEqualTo(locationUpdate.getOrderId());
        assertThat(publishedEvent.getLatitude()).isEqualTo(locationUpdate.getLatitude());
        assertThat(publishedEvent.getLongitude()).isEqualTo(locationUpdate.getLongitude());
        assertThat(publishedEvent.getTimestamp()).isCloseTo(Instant.now(), org.assertj.core.api.Assertions.within(10, ChronoUnit.SECONDS));
    }

    private String generateTestJwt(String username, String role) {
        return Jwts.builder()
                .addClaims(Map.of("roles", List.of(role)))
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(TEST_SECRET_KEY)
                .compact();
    }
}