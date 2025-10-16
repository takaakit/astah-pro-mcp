package com.astahpromcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Utility for converting request arguments into DTOs and validating them using JSON Schema validation.
@Slf4j
public final class ValidationSupport {

    private ValidationSupport() {
    }

    private static final JsonSchemaFactory SCHEMA_FACTORY =
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    // Cache results to improve performance
    private static final Map<Class<?>, JsonSchema> SCHEMA_CACHE = new ConcurrentHashMap<>();

    // Converts the given argument map into the specified DTO and validates it.
    public static <T> ValidationResult<T> parse(Map<String, Object> args, Class<T> type) {
        log.debug("=== VALIDATION START ===");
        log.debug("Input arguments: {}", args);
        log.debug("Target type: {}", type.getSimpleName());
        
        try {
            // Convert the arguments into the DTO
            T dto = JsonSupport.OBJ_MAPPER.convertValue(args, type);
            log.debug("Converted DTO: {}", dto);
            
            // Retrieve the JSON schema from the cache or generate and cache it
            JsonSchema schema = SCHEMA_CACHE.computeIfAbsent(type, k -> {
                JsonNode schemaNode = SchemaSupport.generateSchemaNode(k);
                log.debug("Generated schema for {}: {}", k.getSimpleName(), schemaNode);
                return SCHEMA_FACTORY.getSchema(schemaNode);
            });
            
            // Convert the DTO into a JsonNode
            JsonNode jsonNode = JsonSupport.OBJ_MAPPER.valueToTree(dto);
            
            // Execute schema validation
            Set<ValidationMessage> validationMessages = schema.validate(jsonNode);
            log.info("Validation messages count: {}", validationMessages.size());
            
            // If validation messages are not empty, return an error response
            if (!validationMessages.isEmpty()) {
                String message = validationMessages.stream()
                        .map(ValidationMessage::getMessage)
                        .collect(Collectors.joining(", "));
                log.error("Validation failed message={}", message);
                log.error("Validation details:");
                validationMessages.forEach(msg -> log.error("  - {}", msg.getMessage()));
                McpSchema.CallToolResult error = ResponseSupport.error(message);

                log.debug("=== VALIDATION FAILED ===");
                return new ValidationResult<>(null, error);
            }
            
            log.debug("=== VALIDATION SUCCESS ===");
            return new ValidationResult<>(dto, null);
            
        } catch (Exception e) {
            log.error("Validation error occurred", e);
            McpSchema.CallToolResult error = ResponseSupport.error("Validation error: " + e.getMessage());
            
            log.debug("=== VALIDATION ERROR ===");
            return new ValidationResult<>(null, error);
        }
    }

    // Result object carrying either a DTO or an error response.
    public record ValidationResult<T>(
        T dto,
        McpSchema.CallToolResult error
    ) {
    }
}
