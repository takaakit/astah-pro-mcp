package com.astahpromcp.tool;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ToolSupportTest {

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
