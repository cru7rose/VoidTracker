package com.example.order_service.service;

import com.example.order_service.entity.AssetDefinitionEntity;
import com.example.order_service.repository.AssetDefinitionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemaValidationService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaValidationService.class);

    private final AssetDefinitionRepository assetDefinitionRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public void validate(String code, String json) {
        log.debug("Validating JSON against schema code: {}", code);

        AssetDefinitionEntity definition = assetDefinitionRepository.findLatestByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Schema not found for code: " + code));

        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(definition.getSchema());

            Set<ValidationMessage> errors = schema.validate(jsonNode);

            if (!errors.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("JSON validation failed:");
                for (ValidationMessage error : errors) {
                    errorMessage.append(" ").append(error.getMessage()).append(";");
                }
                throw new IllegalArgumentException(errorMessage.toString());
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IllegalArgumentException("Invalid JSON format or Schema error", e);
        }
    }
}
