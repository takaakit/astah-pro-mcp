package com.astahpromcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionConfig;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
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

    private static final SchemaRegistry SCHEMA_REGISTRY =
        SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
    // Cache compiled schemas for each DTO type
    private static final Map<Class<?>, Schema> SCHEMA_CACHE = new ConcurrentHashMap<>();

    // Converts the given argument map into the specified DTO and validates it.
    public static <T> ValidationResult<T> parse(Map<String, Object> args, Class<T> type) {
        log.debug("=== VALIDATION START ===");
        log.debug("Input arguments: {}", args);
        log.debug("Target type: {}", type.getSimpleName());
        
        try {
            // Convert the arguments into the DTO
            T dto = JsonSupport.OBJ_MAPPER.convertValue(args, type);
            log.debug("Converted DTO: {}", dto);
            
            // Retrieve or build the compiled schema for the DTO type
            Schema schema = SCHEMA_CACHE.computeIfAbsent(type, k -> {
                JsonNode schemaNode = SchemaSupport.generateSchemaNode(k);
                log.debug("Generated schema for {}: {}", k.getSimpleName(), schemaNode);
                return SCHEMA_REGISTRY.getSchema(schemaNode);
            });
            
            // Convert the DTO into a JsonNode
            JsonNode jsonNode = JsonSupport.OBJ_MAPPER.valueToTree(dto);
            
            // Execute schema validation
            ExecutionContext executionContext = new ExecutionContext(ExecutionConfig.getInstance());
            schema.validate(executionContext, jsonNode);
            Set<Error> validationErrors = Set.copyOf(executionContext.getErrors());
            log.info("Validation errors count: {}", validationErrors.size());
            
            // If validation messages are not empty, return an error response
            if (!validationErrors.isEmpty()) {
                String message = validationErrors.stream()
                        .map(error -> error.getInstanceLocation() + ": " + error.getMessage())
                        .collect(Collectors.joining(", "));
                log.error("Validation failed message={}", message);
                log.error("Validation details:");
                validationErrors.forEach(err -> log.error("  - {}", err));
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
