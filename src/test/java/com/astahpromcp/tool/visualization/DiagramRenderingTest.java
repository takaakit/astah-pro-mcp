package com.astahpromcp.tool.visualization;

import com.astahpromcp.config.McpServerConfig;
import com.astahpromcp.tool.ToolDefinition;
import com.astahpromcp.tool.common.ImageConvertSupport;
import io.modelcontextprotocol.spec.McpSchema;
import net.sourceforge.plantuml.dot.ExeState;
import net.sourceforge.plantuml.dot.GraphvizRuntimeEnvironment;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import java.util.Map;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class DiagramRenderingTest {

    @Test
    void generateDiagramImageFromPlantuml_ok_returnsDecodablePng() throws Exception {
        ToolDefinition definition = new PlantumlTool().createToolDefinitions().getFirst();
        assertPng(definition, Map.of("plantumlCode", "@startuml\nAlice -> Bob : Hello\n@enduml"));
    }

    @Test
    void generateGraphImageFromDot_ok_convertsSvgToPng() throws Exception {
        File dot = GraphvizRuntimeEnvironment.getInstance().getDotExe();
        assumeTrue(dot != null && ExeState.checkFile(dot) == ExeState.OK, "Graphviz is not installed");
        ToolDefinition definition = new GraphvizTool(new ImageConvertSupport()).createToolDefinitions().getFirst();
        assertPng(definition, Map.of("dotCode", "digraph G { Alice -> Bob; }"));
    }

    @Test
    void generateGraphImageFromDot_ok_shrinksAGraphAboveThePixelLimitAndSaysByHowMuch() throws Exception {
        File dot = GraphvizRuntimeEnvironment.getInstance().getDotExe();
        assumeTrue(dot != null && ExeState.checkFile(dot) == ExeState.OK, "Graphviz is not installed");
        ToolDefinition definition = new GraphvizTool(new ImageConvertSupport()).createToolDefinitions().getFirst();

        // One 100 x 100 inch node: 9600 x 9600 pixels at Batik's 96 dpi, above the limit of 4096 x 4096
        McpSchema.CallToolResult result = definition.toolHandler().apply(null, new McpSchema.CallToolRequest(
                definition.toolSchema().name(),
                Map.of("dotCode", "digraph G { n [shape=box, width=100, height=100, fixedsize=true]; }"),
                null));

        assertFalse(Boolean.TRUE.equals(result.isError()), () -> result.content().toString());
        assertEquals(2, result.content().size());
        McpSchema.TextContent note = assertInstanceOf(McpSchema.TextContent.class, result.content().get(0));
        assertTrue(note.text().contains("% of its natural size"), note.text());

        McpSchema.ImageContent content = assertInstanceOf(McpSchema.ImageContent.class, result.content().get(1));
        try (ByteArrayInputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(content.data()))) {
            BufferedImage image = ImageIO.read(input);
            assertTrue((long) image.getWidth() * image.getHeight() <= McpServerConfig.SVG_MAX_RASTER_PIXELS,
                    image.getWidth() + "x" + image.getHeight());
        }
    }

    private static void assertPng(ToolDefinition definition, Map<String, Object> arguments) throws Exception {
        McpSchema.CallToolResult result = definition.toolHandler().apply(null,
                new McpSchema.CallToolRequest(definition.toolSchema().name(), arguments, null));
        assertFalse(Boolean.TRUE.equals(result.isError()), () -> result.content().toString());
        assertEquals(1, result.content().size());
        McpSchema.ImageContent content = assertInstanceOf(McpSchema.ImageContent.class, result.content().getFirst());
        assertEquals("image/png", content.mimeType());
        try (ByteArrayInputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(content.data()))) {
            BufferedImage image = ImageIO.read(input);
            assertNotNull(image);
            assertTrue(image.getWidth() > 20);
            assertTrue(image.getHeight() > 20);
        }
    }
}
