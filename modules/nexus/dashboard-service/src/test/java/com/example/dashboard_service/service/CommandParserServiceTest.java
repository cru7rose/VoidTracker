package com.example.dashboard_service.service;

import com.example.dashboard_service.dto.CommandResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserServiceTest {

    private final CommandParserService service = new CommandParserService();

    @Test
    void testNavigateToOrders() {
        CommandResponse response = service.parseCommand("show me the orders");
        assertEquals("NAVIGATE", response.getAction());
        assertEquals("/internal/orders", response.getTarget());
    }

    @Test
    void testNavigateToMap() {
        CommandResponse response = service.parseCommand("open live map");
        assertEquals("NAVIGATE", response.getAction());
        assertEquals("/internal/map", response.getTarget());
    }

    @Test
    void testTrackDriver() {
        CommandResponse response = service.parseCommand("track driver D101");
        assertEquals("NAVIGATE", response.getAction());
        assertEquals("/internal/map?driver=D101", response.getTarget());
    }

    @Test
    void testTrackDriverSynonym() {
        CommandResponse response = service.parseCommand("where is D101");
        assertEquals("NAVIGATE", response.getAction());
        assertEquals("/internal/map?driver=D101", response.getTarget());
    }

    @Test
    void testRiskLens() {
        CommandResponse response = service.parseCommand("show risk");
        assertEquals("NAVIGATE", response.getAction());
        assertEquals("/internal/map?layer=risk", response.getTarget());
    }

    @Test
    void testProfitLens() {
        CommandResponse response = service.parseCommand("show profit map");
        assertEquals("NAVIGATE", response.getAction());
        assertEquals("/internal/map?layer=profit", response.getTarget());
    }

    @Test
    void testUnknownCommand() {
        CommandResponse response = service.parseCommand("hello world");
        assertEquals("MESSAGE", response.getAction());
        assertNotNull(response.getMessage());
    }
}
