package com.astahpromcp.tool;

import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ToolSupportTest {

    private record TestOutputDTO(String value) {
    }

    @Test
    void toolDefinitionReturningDto_ok_toolFunctionDoesNotRequireExchange() {
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDto(
                "test_dto",
                "test",
                input -> new TestOutputDTO("dto"),
                NoInputDTO.class,
                TestOutputDTO.class);

        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto", Map.of()));

        assertFalse(result.isError());
        assertEquals(Map.of("value", "dto"), result.structuredContent());
    }

    @Test
    void toolDefinitionReturningContents_ok_toolFunctionDoesNotRequireExchange() {
        McpSchema.TextContent text = McpSchema.TextContent.builder("contents").build();
        ToolDefinition definition = ToolSupport.toolDefinitionReturningContents(
                "test_contents",
                "test",
                input -> List.of(text),
                NoInputDTO.class);

        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_contents", Map.of()));

        assertFalse(result.isError());
        assertEquals(List.of(text), result.content());
    }

    @Test
    void toolDefinitionReturningDtoAndContents_ok_toolFunctionDoesNotRequireExchange() {
        McpSchema.TextContent text = McpSchema.TextContent.builder("contents").build();
        ToolDefinition definition = ToolSupport.toolDefinitionReturningDtoAndContents(
                "test_dto_and_contents",
                "test",
                input -> Pair.of(new TestOutputDTO("dto"), List.of(text)),
                NoInputDTO.class,
                TestOutputDTO.class);

        McpSchema.CallToolResult result = definition.toolHandler().apply(
                null,
                new McpSchema.CallToolRequest("test_dto_and_contents", Map.of()));

        assertFalse(result.isError());
        assertEquals(Map.of("value", "dto"), result.structuredContent());
        assertEquals(List.of(text), result.content());
    }

    @Test
    void summarizeContents_ok_imageContentIsReducedToItsSize() {
        String base64Data = "iVBORw0KGgo=".repeat(1000);  // pseudo large Base64 payload
        McpSchema.ImageContent image = McpSchema.ImageContent.builder(base64Data, "image/png").build();

        String summary = ToolSupport.summarizeContents(List.of(image));

        // The raw Base64 data must not be dumped into the log
        assertFalse(summary.contains(base64Data));
        // The summary must carry the information useful for troubleshooting
        assertTrue(summary.contains("image/png"));
        assertTrue(summary.contains(base64Data.length() + " chars"));
    }

    @Test
    void summarizeContents_ok_textContentIsKeptAsIs() {
        McpSchema.TextContent text = McpSchema.TextContent.builder("{\"id\":\"abc\"}").build();

        String summary = ToolSupport.summarizeContents(List.of(text));

        assertTrue(summary.contains("{\"id\":\"abc\"}"));
    }

    @Test
    void summarizeContents_ng_null() {
        assertEquals("null", ToolSupport.summarizeContents(null));
    }
}
