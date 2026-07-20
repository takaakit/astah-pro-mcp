package com.astahpromcp.tool;

import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithAbstractDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaSupportTest {

    @Test
    void generateSchema_marksAllInputPropertiesAsRequired() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(ClassWithAbstractDTO.class));

        JsonNode required = schema.get("required");
        assertNotNull(required, "Schema must declare a required array");
        assertTrue(required.isArray());
        assertEquals(schema.get("properties").size(), required.size(),
            "Every property must be required");
    }

    @Test
    void generateSchema_requiresIdProperty() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(IdDTO.class));

        JsonNode required = schema.get("required");
        assertNotNull(required);
        assertEquals(1, required.size());
        assertEquals("id", required.get(0).asString());
    }

    @Test
    void generateSchema_noRequiredForEmptyInput() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(NoInputDTO.class));

        assertNull(schema.get("required"), "An empty DTO must not declare required properties");
    }

    @Test
    void generateSchema_marksAllOutputPropertiesAsRequired() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(ClassDTO.class));

        JsonNode required = schema.get("required");
        assertNotNull(required, "Output schema must declare a required array");
        assertEquals(schema.get("properties").size(), required.size(),
            "Every output property must be required");
    }
}
