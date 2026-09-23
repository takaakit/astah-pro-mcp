package com.astahpromcp.tool;

import tools.jackson.databind.JsonNode;
import com.fasterxml.classmate.ResolvedType;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonSchemaModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;

import java.util.Map;

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

        // A Map is written where it is used, never as a named definition in $defs.
        // Without this, a DTO using the same Map type twice would get a definition named like "Map(String,Object)".
        configBuilder.forTypesInGeneral().withCustomDefinitionProvider(new CustomDefinitionProviderV2() {
            @Override
            public CustomDefinition provideCustomSchemaDefinition(ResolvedType javaType, SchemaGenerationContext context) {
                if (!Map.class.isAssignableFrom(javaType.getErasedType())) {
                    return null;
                }
                // Passing this provider makes the standard definition skip it, rather than come back here
                return new CustomDefinition(context.createStandardDefinition(javaType, this),
                    CustomDefinition.DefinitionType.INLINE, CustomDefinition.AttributeInclusion.YES);
            }
        });

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
