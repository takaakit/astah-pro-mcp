package com.astahpromcp.tool;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseSupportTest {

    private record TestDto(String value) {
    }

    @Test
    void success_ok_dtoIsReturnedAsStructuredContentOnly() {
        McpSchema.CallToolResult result = ResponseSupport.success(new TestDto("abc"));

        assertFalse(result.isError());
        assertTrue(result.content().isEmpty());
        assertEquals(Map.of("value", "abc"), result.structuredContent());
    }

    @Test
    void success_ok_contentsAreReturnedAsUnstructuredContentOnly() {
        McpSchema.TextContent text = McpSchema.TextContent.builder("plain text").build();

        McpSchema.CallToolResult result = ResponseSupport.success(List.of(text));

        assertFalse(result.isError());
        assertEquals(List.of(text), result.content());
        assertNull(result.structuredContent());
    }

    @Test
    void success_ok_dtoAndImageDoNotDuplicateDtoAsText() {
        McpSchema.ImageContent image = McpSchema.ImageContent.builder("base64", "image/png").build();

        McpSchema.CallToolResult result = ResponseSupport.success(new TestDto("abc"), List.of(image));

        assertFalse(result.isError());
        assertEquals(Map.of("value", "abc"), result.structuredContent());
        assertEquals(List.of(image), result.content());
        assertTrue(result.content().stream().noneMatch(McpSchema.TextContent.class::isInstance));
    }

    @Test
    void success_ok_nullContentsReturnsStructuredContentOnly() {
        McpSchema.CallToolResult result = ResponseSupport.success(new TestDto("abc"), null);

        assertFalse(result.isError());
        assertTrue(result.content().isEmpty());
        assertEquals(Map.of("value", "abc"), result.structuredContent());
    }
}
