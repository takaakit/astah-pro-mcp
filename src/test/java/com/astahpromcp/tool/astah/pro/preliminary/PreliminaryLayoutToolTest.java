package com.astahpromcp.tool.astah.pro.preliminary;

import com.astahpromcp.tool.astah.pro.TestSupport;
import com.astahpromcp.tool.common.inputdto.NoInputDTO;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreliminaryLayoutToolTest {

    private PreliminaryLayoutTool tool;
    private Method getSteps;

    @BeforeEach
    void setUp() throws Exception {
        // Tool
        tool = new PreliminaryLayoutTool(true);

        // getSteps() method
        getSteps = TestSupport.getAccessibleMethod(
            PreliminaryLayoutTool.class,
            "getSteps",
            NoInputDTO.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Cleanup if needed
    }

    // The tool holds no Astah state, so the method is invoked directly rather than through TestSupport.instance(),
    // which would need the Astah API.
    @SuppressWarnings("unchecked")
    private List<McpSchema.Content> invokeGetSteps() throws Exception {
        return (List<McpSchema.Content>) getSteps.invoke(tool, new NoInputDTO());
    }

    @Test
    void getSteps_ok() throws Exception {
        // ----------------------------------------
        // Call getSteps()
        // ----------------------------------------
        List<McpSchema.Content> contents = invokeGetSteps();

        // Check the steps, which come first
        assertNotNull(contents);
        McpSchema.TextContent steps = assertInstanceOf(McpSchema.TextContent.class, contents.get(0));
        assertFalse(steps.text().isBlank());

        // The output directory is interpolated into the steps, so no format specifier may survive
        assertFalse(steps.text().contains("%1$s"));
        assertTrue(steps.text().contains("preliminary-layout"));
    }

    @Test
    void getSteps_ok_svgAndPngExamplePerDiagramType() throws Exception {
        // ----------------------------------------
        // Call getSteps()
        // ----------------------------------------
        List<McpSchema.Content> contents = invokeGetSteps();

        // The steps, then six diagram types each contributing its SVG code followed by the PNG image it renders to
        assertNotNull(contents);
        assertEquals(13, contents.size());
        for (int i = 1; i < contents.size(); i += 2) {
            McpSchema.TextContent svgCode = assertInstanceOf(McpSchema.TextContent.class, contents.get(i));
            assertTrue(svgCode.text().contains("<svg"));
            assertInstanceOf(McpSchema.ImageContent.class, contents.get(i + 1));
        }
    }

    @Test
    void createToolDefinitions_ok_editToolsOnly() throws Exception {
        // The steps are only useful while editing, so they are withheld from the query-only profile
        assertEquals(0, new PreliminaryLayoutTool(false).createToolDefinitions().size());
        assertEquals(1, new PreliminaryLayoutTool(true).createToolDefinitions().size());
    }
}
