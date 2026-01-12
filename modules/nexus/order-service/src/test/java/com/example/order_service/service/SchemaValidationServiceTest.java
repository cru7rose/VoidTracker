package com.example.order_service.service;

import com.example.order_service.entity.AssetDefinitionEntity;
import com.example.order_service.repository.AssetDefinitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemaValidationServiceTest {

    @Mock
    private AssetDefinitionRepository assetDefinitionRepository;

    private ObjectMapper objectMapper;

    private SchemaValidationService schemaValidationService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        schemaValidationService = new SchemaValidationService(assetDefinitionRepository, objectMapper);
    }

    @Test
    void validate_shouldPass_whenJsonMatchesSchema() {
        // Given
        String code = "TEST_SCHEMA";
        String schema = "{\"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"object\", \"properties\": {\"age\": {\"type\": \"integer\"}}, \"required\": [\"age\"]}";
        String json = "{\"age\": 25}";

        AssetDefinitionEntity def = new AssetDefinitionEntity();
        def.setCode(code);
        def.setSchema(schema);

        when(assetDefinitionRepository.findLatestByCode(code)).thenReturn(Optional.of(def));

        // When/Then
        assertDoesNotThrow(() -> schemaValidationService.validate(code, json));
    }

    @Test
    void validate_shouldFail_whenJsonDoesNotMatchSchema() {
        // Given
        String code = "TEST_SCHEMA";
        String schema = "{\"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"object\", \"properties\": {\"age\": {\"type\": \"integer\"}}, \"required\": [\"age\"]}";
        String json = "{\"name\": \"John\"}"; // Missing age

        AssetDefinitionEntity def = new AssetDefinitionEntity();
        def.setCode(code);
        def.setSchema(schema);

        when(assetDefinitionRepository.findLatestByCode(code)).thenReturn(Optional.of(def));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> schemaValidationService.validate(code, json));
    }
}
