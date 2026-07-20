package com.astahpromcp.tool;

import tools.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonSchemaModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;

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
        configBuilder
            .with(new JacksonSchemaModule(JacksonOption.FLATTENED_ENUMS_FROM_JSONPROPERTY))
            .without(Option.SCHEMA_VERSION_INDICATOR)      // Remove the $schema field
            .without(Option.DEFINITIONS_FOR_ALL_OBJECTS);  // Suppress automatic $defs generation
            // Note: INLINE_ALL_SCHEMAS cannot be used because schemas are recursive

        // Every property is required
        configBuilder.forFields().withRequiredCheck(field -> true);

        return new SchemaGenerator(configBuilder.build());
    }

    // Generate a formatted JSON schema from a record type
    public static String generateSchema(Class<?> recordClass) {
        JsonNode schemaNode = GENERATOR.generateSchema(recordClass);
        try {
            return JsonSupport.OBJ_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schema for " + recordClass.getName(), e);
        }
    }
}
