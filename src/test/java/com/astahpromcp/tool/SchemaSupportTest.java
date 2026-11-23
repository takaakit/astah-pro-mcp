package com.astahpromcp.tool;

import com.astahpromcp.tool.astah.pro.common.inputdto.IdDTO;
import com.astahpromcp.tool.astah.pro.model.inputdto.ClassWithAbstractDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ClassDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.ElementDTO;
import com.astahpromcp.tool.astah.pro.model.outputdto.NamedElementDTO;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
        
        assertEveryComponentIsARequiredProperty(ClassWithAbstractDTO.class, schema);
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
        
        assertEveryComponentIsARequiredProperty(ClassDTO.class, schema);
        
        // A nested DTO is marked by the same rule, wherever the generator writes it
        assertEveryComponentIsARequiredProperty(NamedElementDTO.class, resolve(schema, schema.at("/properties/namedElement")));
    }
    
    // Checks that the properties of the schema are the components of the record and that each of them is required once.
    // The names expected come from the record rather than from the schema, so that a property missing from both is noticed too.
    private static void assertEveryComponentIsARequiredProperty(Class<? extends Record> dtoClass, JsonNode schema) {
        Set<String> components = Arrays.stream(dtoClass.getRecordComponents())
            .map(RecordComponent::getName)
            .collect(Collectors.toCollection(TreeSet::new));
        assertFalse(components.isEmpty(), dtoClass.getSimpleName() + " has no component to check");
        
        JsonNode properties = schema.get("properties");
        assertNotNull(properties, "Schema must declare its properties: " + schema);
        assertTrue(properties.isObject(), "properties must be an object: " + properties);
        assertEquals(components, new TreeSet<>(properties.propertyNames()), "properties of " + dtoClass.getSimpleName());
        
        JsonNode required = schema.get("required");
        assertNotNull(required, "Schema must declare a required array: " + schema);
        assertTrue(required.isArray(), "required must be an array: " + required);
        List<String> requiredNames = new ArrayList<>();
        for (JsonNode name : required) {
            assertTrue(name.isString(), "required must hold property names: " + required);
            requiredNames.add(name.asString());
        }
        assertEquals(components, new TreeSet<>(requiredNames), "required of " + dtoClass.getSimpleName());
        assertEquals(components.size(), requiredNames.size(), "A property is required more than once: " + required);
    }
    
    // Returns the schema the node stands for, following a reference into the definitions
    private static JsonNode resolve(JsonNode schema, JsonNode node) {
        assertFalse(node.isMissingNode(), "No such node in " + schema);
        JsonNode ref = node.get("$ref");
        if (ref == null) {
            return node;
        }
        String pointer = ref.asString();
        assertTrue(pointer.startsWith("#/"), "Unexpected reference: " + pointer);
        JsonNode target = schema.at(pointer.substring(1));
        assertFalse(target.isMissingNode(), "The reference " + pointer + " names nothing in " + schema);
        return target;
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
    void generateSchema_ok_generatesEachTypeOnlyOnce() {
        String first = SchemaSupport.generateSchema(IdDTO.class);
        String second = SchemaSupport.generateSchema(IdDTO.class);
        
        assertSame(first, second, "The second call must reuse the schema generated by the first");
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
