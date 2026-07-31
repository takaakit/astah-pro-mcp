package com.astahpromcp.tool;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSupportTest {

    private record DtoWithNullableFields(
        String name,
        List<String> items
    ) {
    }

    @Test
    void writeValueAsString_ok_nullFieldsAreSerializedWithKeysPresent() {
        String json = JsonSupport.OBJ_MAPPER.writeValueAsString(new DtoWithNullableFields(null, null));
        JsonNode node = JsonSupport.OBJ_MAPPER.readTree(json);

        assertTrue(node.has("name"), "Null fields must keep their keys in the serialized JSON");
        assertTrue(node.get("name").isNull());
        assertTrue(node.has("items"), "Null fields must keep their keys in the serialized JSON");
        assertTrue(node.get("items").isNull());
    }
}
