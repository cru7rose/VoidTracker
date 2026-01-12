// Plik: order-service/src/test/java/com/example/order_service/FullFlowIntegrationTest.java
package com.example.order_service;

import com.example.danxils_commons.dto.AddressDto;
import com.example.danxils_commons.dto.PackageDto;
import com.example.danxils_commons.enums.OrderStatus;
import com.example.danxils_commons.event.RoutePlannedEvent;
import com.example.danxils_commons.dto.OrderResponseDto;
import com.example.order_service.dto.request.CreateOrderRequestDto;
import com.example.order_service.dto.request.CreateOrderRequestDto;
import com.example.order_service.entity.ClientEntity;
import com.example.order_service.entity.OrderEntity;
import com.example.order_service.repository.ClientRepository;
import com.example.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.liquibase.enabled=true",
        "spring.main.allow-bean-definition-overriding=true" })
@Import(KafkaTestConfig.class)
@AutoConfigureMockMvc
public class FullFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.2"));
    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // *** START OF FIX ***
        // Add the missing Kafka bootstrap server property
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        // *** END OF FIX ***
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("testKafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.route-planned}")
    private String routesPlannedTopic;

    private ClientEntity testClient;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        clientRepository.deleteAll();
        ClientEntity client = new ClientEntity();
        client.setName("Test Customer");
        testClient = clientRepository.save(client);
    }

    @Test
    @WithMockUser
    void shouldProcessOrderFromCreationToPlannedAndAssigned() throws Exception {
        // --- ETAP 1: Utwórz zlecenie przez API ---
        CreateOrderRequestDto createOrderRequest = createOrderRequest(testClient.getId());
        MvcResult result = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID orderId = UUID.fromString(responseJson.get("orderId").asText());

        // --- ETAP 2: Sprawdź, czy zlecenie jest w bazie w stanie NEW ---
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            OrderEntity order = orderRepository.findById(orderId).orElseThrow();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
            assertThat(order.getAssignedDriver()).isNull();
        });
        // --- ETAP 3: Symuluj zdarzenie z planning-service ---
        UUID vehicleId = UUID.randomUUID();
        RoutePlannedEvent routePlannedEvent = RoutePlannedEvent.builder()
                .planId(UUID.randomUUID())
                .vehicleId(vehicleId)
                .includedOrderIds(List.of(orderId))
                .createdAt(Instant.now())
                .build();
        kafkaTemplate.send(routesPlannedTopic, routePlannedEvent);

        // --- ETAP 4: Czekaj i sprawdź, czy status zlecenia zmienił się na PICKUP ---
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            OrderEntity updatedOrder = orderRepository.findById(orderId).orElseThrow();

            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PICKUP);
            assertThat(updatedOrder.getAssignedDriver()).isEqualTo(vehicleId.toString());
        });
    }

    @Test
    @WithMockUser
    void shouldCreateTransferOrderWithPendingStatus() throws Exception {
        // given
        CreateOrderRequestDto request = createOrderRequest(testClient.getId(), true);

        // when
        MvcResult result = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        OrderResponseDto response = objectMapper.readValue(result.getResponse().getContentAsString(),
                OrderResponseDto.class);
        assertThat(response.orderId()).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
    }

    private CreateOrderRequestDto createOrderRequest(UUID customerId) {
        return createOrderRequest(customerId, false);
    }

    private CreateOrderRequestDto createOrderRequest(UUID customerId, boolean isTransfer) {
        AddressDto pickup = AddressDto.builder()
                .street("Pickup St").streetNumber("1").city("Warsaw").postalCode("00-001")
                .customerName("John").phone("123456789").build();
        CreateOrderRequestDto.DeliveryAddressDto delivery = new CreateOrderRequestDto.DeliveryAddressDto(
                "Client", "Attn", "Delivery St", "2", "00-002", "Krakow", "PL", null, "987654321", null, 52.0, 21.0,
                Instant.now().plusSeconds(3600));
        PackageDto packageDetails = PackageDto.builder()
                .barcode1("BARCODE123").weight(10.5).volume(0.1).build();
        return new CreateOrderRequestDto(customerId.toString(), "NORMAL", null, pickup, delivery, packageDetails, null,
                null,
                null,
                null,
                null,
                isTransfer);
    }
}