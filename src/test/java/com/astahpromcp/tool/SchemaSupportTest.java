package com.astahpromcp.tool;

import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithAbstractDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaSupportTest {

    // Uses one Map type twice, which is what would otherwise make the generator give the Map a named definition
    private record TwoMapsDTO(
        @JsonPropertyDescription("First map")
        Map<String, Object> first,

        @JsonPropertyDescription("Second map")
        Map<String, Object> second
    ) {
    }

    @Test
    void generateSchema_ok_marksAllInputPropertiesAsRequired() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(ClassWithAbstractDTO.class));

        JsonNode required = schema.get("required");
        assertNotNull(required, "Schema must declare a required array");
        assertTrue(required.isArray());
        assertEquals(schema.get("properties").size(), required.size(),
            "Every property must be required");
    }

    @Test
    void generateSchema_ok_requiresIdProperty() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(IdDTO.class));

        JsonNode required = schema.get("required");
        assertNotNull(required);
        assertEquals(1, required.size());
        assertEquals("id", required.get(0).asString());
    }

    @Test
    void generateSchema_ok_noRequiredForEmptyInput() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(NoInputDTO.class));

        assertNull(schema.get("required"), "An empty DTO must not declare required properties");
    }

    @Test
    void generateSchema_ok_marksAllOutputPropertiesAsRequired() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(ClassDTO.class));

        JsonNode required = schema.get("required");
        assertNotNull(required, "Output schema must declare a required array");
        assertEquals(schema.get("properties").size(), required.size(),
            "Every output property must be required");
    }

    @Test
    void generateSchema_ok_writesAMapWhereItIsUsedEvenWhenItIsUsedTwice() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(TwoMapsDTO.class));

        assertNull(schema.get("$defs"), "A Map must not become a named definition: " + schema);
        assertEquals("object", schema.at("/properties/first/type").asString());
        assertEquals("First map", schema.at("/properties/first/description").asString());
        assertEquals("object", schema.at("/properties/second/type").asString());
        assertEquals("Second map", schema.at("/properties/second/description").asString());
    }

    @Test
    void generateSchema_ok_writesAMapUsedOnceAsBefore() {
        JsonNode schema = JsonSupport.OBJ_MAPPER.readTree(
            SchemaSupport.generateSchema(ElementDTO.class));

        assertNull(schema.get("$defs"), "A Map must not become a named definition: " + schema);
        assertEquals(JsonSupport.OBJ_MAPPER.readTree("{\"type\":\"object\",\"description\":\"Tagged values\"}"),
            schema.at("/properties/taggedValues"));
    }
}
