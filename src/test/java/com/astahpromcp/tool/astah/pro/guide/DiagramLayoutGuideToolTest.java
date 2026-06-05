package com.astahpromcp.tool.astah.pro.guide;

import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DiagramLayoutGuideToolTest {

    private DiagramLayoutGuideTool tool;
    private Method getGuide;

    @BeforeEach
    void setUp() throws Exception {
        // Tool
        tool = new DiagramLayoutGuideTool();

        // getGuide() method
        getGuide = TestSupport.getAccessibleMethod(
            DiagramLayoutGuideTool.class,
            "getGuide",
            McpSyncServerExchange.class,
            NoInputDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Cleanup if needed
    }

    @Test
    void getGuide_ok() throws Exception {
        // Create input DTO
        NoInputDTO inputDTO = new NoInputDTO();

        // ----------------------------------------
        // Call getGuide()
        // ----------------------------------------
        List<McpSchema.Content> contents = TestSupport.instance().invokeToolMethodReturningContents(
            getGuide,
            tool,
            inputDTO);

        // Check the contents
        assertNotNull(contents);
        assertEquals(6, contents.size());
        assertInstanceOf(McpSchema.TextContent.class, contents.get(0));
        assertInstanceOf(McpSchema.ImageContent.class, contents.get(1));
        assertInstanceOf(McpSchema.ImageContent.class, contents.get(2));
        assertInstanceOf(McpSchema.ImageContent.class, contents.get(3));
        assertInstanceOf(McpSchema.ImageContent.class, contents.get(4));
        assertInstanceOf(McpSchema.ImageContent.class, contents.get(5));
    }
}
