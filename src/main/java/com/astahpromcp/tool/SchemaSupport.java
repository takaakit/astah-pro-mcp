package com.astahpromcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;

// JSON schema utility
public final class SchemaSupport {
    
    private static final SchemaGenerator GENERATOR = createSchemaGenerator();

    private SchemaSupport() {
    }

    private static SchemaGenerator createSchemaGenerator() {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
            SchemaVersion.DRAFT_2020_12,
            OptionPreset.PLAIN_JSON);

        // Configure for simpler JSON output
        return new SchemaGenerator(
            configBuilder
                .with(new JacksonModule())
                .without(Option.SCHEMA_VERSION_INDICATOR)      // Remove the $schema field
                .without(Option.DEFINITIONS_FOR_ALL_OBJECTS)   // Suppress automatic $defs generation
                // Note: INLINE_ALL_SCHEMAS cannot be used because schemas are recursive
                .build());
    }

    static JsonNode generateSchemaNode(Class<?> type) {
        return GENERATOR.generateSchema(type);
    }
        
    // Generate a formatted JSON schema from a record type
    public static String generateSchema(Class<?> recordClass) {
        JsonNode schemaNode = generateSchemaNode(recordClass);
        try {
            return JsonSupport.OBJ_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schema for " + recordClass.getName(), e);
        }
    }
}
