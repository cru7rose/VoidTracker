package com.example.order_service;

import com.example.order_service.dto.request.WmsOrderRequestDto;
import com.example.order_service.entity.ClientEntity;
import com.example.order_service.entity.OrderEntity;
import com.example.order_service.repository.ClientRepository;
import com.example.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = { "orders.created", "orders.status-changed", "orders.assigned" })
class WmsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ClientEntity testClient;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        clientRepository.deleteAll();

        testClient = new ClientEntity();
        testClient.setName("BMW_ALIAS");
        testClient.addProperty("email", "test@bmw.com");
        clientRepository.save(testClient);
    }

    @Test
    void shouldCreateOrderFromWmsPayload() throws Exception {
        // Given
        WmsOrderRequestDto request = new WmsOrderRequestDto(
                "BMW_ALIAS",
                "SN123456",
                "Wheel 12.5 kg",
                4,
                Instant.now());

        // When
        String response = mockMvc.perform(post("/api/wms/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Then
        UUID orderId = UUID.fromString(response.replace("\"", ""));
        OrderEntity savedOrder = orderRepository.findById(orderId).orElseThrow();

        assertEquals(testClient.getId(), savedOrder.getOrderingCustomer().getId());
        assertEquals("SN123456", savedOrder.getPartNumber());
        assertEquals(4, savedOrder.getAssets().get(0).getAttributes().get("colli"));
        assertEquals(12.5, savedOrder.getAssets().get(0).getAttributes().get("weight")); // Verified weight parsing
        assertNotNull(savedOrder.getWarehouseAcceptanceDate());
    }
}
